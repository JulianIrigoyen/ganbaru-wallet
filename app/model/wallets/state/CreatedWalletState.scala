package model.wallets.state

import java.time.LocalDateTime

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.ActorContext
import model.{Account, AccountId, Money, Transaction, TransactionId}
import model.settings.GandaruServiceSettings
import model.util.{AcknowledgeWithFailure, AcknowledgeWithResult}
import model.wallets.WalletCommands.{AddAccount, AttemptTransaction, Deposit, GetAccount, GetBulkiestAccount, GetWallet, ListTransactions, RollbackTransaction, Withdraw}
import model.wallets.WalletEvents.{AccountAdded, Deposited, TransactionRolledback, TransactionValidated, WalletCreated, Withdrew}
import model.wallets.state.WalletState.{EventsAnswerReplyEffect, NonEventsAnswerReplyEffect, WalletState}
import model.wallets.{CreatedWallet, GandaruClientId, WalletCommands, WalletEvents, WalletId}
import sharding.EntityProvider

case class CreatedWalletState(
                               wallet: CreatedWallet,
                               settings: EntityProvider[GandaruServiceSettings.Command, GandaruClientId]
                        ) extends WalletState {

  override def applyCommand(command: WalletCommands.Command)(implicit context: ActorContext[WalletCommands.Command]): WalletState.EventsAnswerEffect = {
    implicit val as: ActorSystem[Nothing] = context.system

    command match {

      case GetWallet(replyTo) => new EventsAnswerReplyEffect(this, Nil, replyTo, _ => AcknowledgeWithResult(wallet))

      case addAcc @ AddAccount(cuit, accountType, currency, replyTo) =>
        println(s"Creating wallet account")
        wallet.accounts.collectFirst {
          case acc: Account if acc.accountType == accountType && acc.balance.currency == currency => acc
        } match {
          case Some(_) => new NonEventsAnswerReplyEffect[AcknowledgeWithFailure[AccountId]](replyTo,
            AcknowledgeWithFailure(s"User with cuit $cuit already has a $accountType account in $currency . "))
          case None =>
            val newAccountId = AccountId.newAccountId
            val event = List(addAcc.asEvent(wallet, newAccountId))
            new EventsAnswerReplyEffect[AcknowledgeWithResult[AccountId]](this, event, replyTo, _ => AcknowledgeWithResult(newAccountId))
        }

      case GetAccount(accountId, replyTo) =>
        wallet.accounts.find(_.accountId == accountId) match {
          case Some(account) => new EventsAnswerReplyEffect[AcknowledgeWithResult[Account]](this, Nil, replyTo, _ => AcknowledgeWithResult(account))
          case None          => new NonEventsAnswerReplyEffect[AcknowledgeWithFailure[Account]](replyTo, AcknowledgeWithFailure(s"Account $accountId does not exist. "))
        }

      case GetBulkiestAccount(replyTo) =>
        wallet.accounts.size match {
          case x if x == 0 => new NonEventsAnswerReplyEffect[AcknowledgeWithFailure[Account]](replyTo,
              AcknowledgeWithFailure(s"There are now accounts for wallet ${wallet.walletId}"))
          case _ =>
            val bulkiestAccount = wallet.accounts.maxBy(_.balance.amount)
            //println(bulkiestAccount)
            new EventsAnswerReplyEffect[AcknowledgeWithResult[Account]](this, Nil, replyTo, _ => AcknowledgeWithResult(bulkiestAccount))
        }

      case deposit @ Deposit(accountId, amountToDeposit, replyTo) =>
        wallet.accounts.find(_.accountId == accountId) match {
          case Some(account) =>
            val events = List(deposit.asEvent(wallet, account))
            val accountWithDeposit = account.copy(balance = account.balance + amountToDeposit)
            new EventsAnswerReplyEffect[AcknowledgeWithResult[Account]](this, events, replyTo, _ => AcknowledgeWithResult(accountWithDeposit))
          case None => new NonEventsAnswerReplyEffect[AcknowledgeWithFailure[Account]](replyTo,
            AcknowledgeWithFailure(s"Account $accountId does not exist. "))
        }

      case withdraw @ Withdraw(accountId, amount, replyTo) =>
        wallet.accounts.find(_.accountId == accountId) match {
          case Some(account) => account match {
            case _ if account.balance.amount < amount.amount => new NonEventsAnswerReplyEffect[AcknowledgeWithFailure[Account]](replyTo,
                AcknowledgeWithFailure(s"Account $accountId does not have enough balance to withdraw $amount "))
            case _ =>
              val events = List(withdraw.asEvent(wallet, account))
              val accountWithWithdrawal = account.copy(balance = account.balance - amount)
              new EventsAnswerReplyEffect[AcknowledgeWithResult[Account]](this, events, replyTo, _ => AcknowledgeWithResult(accountWithWithdrawal))
          }

          case None =>
            new NonEventsAnswerReplyEffect[AcknowledgeWithFailure[Account]](replyTo,
            AcknowledgeWithFailure(s"Account $accountId does not exist. "))
        }

      case transfer @ AttemptTransaction(debitId, creditId, amount, replyTo) =>
        validateTransaction(debitId, creditId, amount) match {
          case Some(accounts) =>
            val transactionId = TransactionId.newTransactionId
            val events = List(transfer.asEvent(wallet, transactionId, accounts._1, accounts._2))
            new EventsAnswerReplyEffect[AcknowledgeWithResult[TransactionId]](this, events, replyTo, _ => AcknowledgeWithResult(transactionId))
          case None => new NonEventsAnswerReplyEffect[AcknowledgeWithFailure[TransactionId]](replyTo,
            AcknowledgeWithFailure(s"It is not possible to debit from $debitId to credit to $creditId. "))
        }

      case rollback @ RollbackTransaction(transactionId, replyTo) =>
        wallet.transactions.find(_.transactionId == transactionId) match {
          case Some(tx) =>
            val accountToDebit = tx.credited
            val accountToCredit = tx.debited

            validateTransaction(accountToDebit.accountId, accountToCredit.accountId, tx.amount) match {
              case Some(_) =>
                val events = List(rollback.asEvent(wallet, accountToDebit, accountToCredit, tx.amount))
                new EventsAnswerReplyEffect[AcknowledgeWithResult[TransactionId]](this, events, replyTo, _ => AcknowledgeWithResult(transactionId))

              case None =>
                new NonEventsAnswerReplyEffect[AcknowledgeWithFailure[TransactionId]](replyTo,
                  AcknowledgeWithFailure(s"There are not enough funds in ${accountToDebit.accountId} to rollback transaction $transactionId. "))
            }
          case None => new NonEventsAnswerReplyEffect[AcknowledgeWithFailure[TransactionId]](replyTo,
              AcknowledgeWithFailure(s"Transaction $transactionId does not exist (at least in wallet ${wallet.walletId}). "))
        }

      case ListTransactions(accountId, replyTo) =>
        wallet.transactions match {
          case head::tail =>
            val txs = wallet.transactions.filter(acc => acc.credited.accountId == accountId || acc.debited.accountId == accountId ).toList
            new EventsAnswerReplyEffect[AcknowledgeWithResult[List[Transaction]]](this, Nil, replyTo, _ => AcknowledgeWithResult(txs))
          case List()  => new NonEventsAnswerReplyEffect[AcknowledgeWithFailure[List[Transaction]]](replyTo,
            AcknowledgeWithFailure(s"Account $accountId has not yet registered transactions"))
        }
    }
  }

  override def applyEvent(event: WalletEvents.Event): WalletState = event match {

    case _: WalletCreated => this

    case AccountAdded(walletId, gandaruClientId, accountId, cuit, accountType, balance, dateOpened) =>
      val newAccount = Account(walletId, gandaruClientId, accountId, cuit, accountType, balance, dateOpened)
      copy(wallet.copy(accounts = wallet.accounts :+ newAccount))

    case Deposited(_, _, account, amountToDeposit, _) =>
      val accountWithDeposit = credit(account, amountToDeposit).get
      copy(wallet.copy(accounts = wallet.accounts.filterNot(_.accountId == account.accountId) :+ accountWithDeposit))

    case Withdrew(_, _, account, amount, _) =>
      val accountWithWithdrawal = debit(account, amount).get
      copy(wallet.copy(accounts = wallet.accounts.filterNot(_.accountId == account.accountId) :+ accountWithWithdrawal))

    case TransactionValidated(_, _, transactionId, debited, credited, amount, _) =>
      val updatedAccounts: (Account, Account) = executeTransaction(debited, credited, amount).get
      copy(
        wallet.copy(
        accounts = wallet.accounts.filterNot ( acc =>
          acc.accountId == debited.accountId || acc.accountId == credited.accountId
        ) :+ updatedAccounts._1 :+ updatedAccounts._2,
        transactions = wallet.transactions :+ Transaction(transactionId, debited, credited, amount, LocalDateTime.now())
      )
    )

    case TransactionRolledback(_, _, transactionId, accountToDebit, accountToCredit, amount, _) =>
      val updatedAccounts: (Account, Account) = executeTransaction(accountToDebit, accountToCredit, amount).get
      copy(
        wallet.copy(
        accounts = wallet.accounts.filterNot( acc =>
          acc.accountId == accountToDebit.accountId || acc.accountId == accountToCredit.accountId
          ) :+ updatedAccounts._1 :+ updatedAccounts._2,
          transactions = wallet.transactions.filterNot(_.transactionId == transactionId)
        )
      )
  }

  private def debit(account: Account, amountToDebit: Money): Option[Account] = {
    account match {
      case _ if account.balance.amount < amountToDebit.amount => None
      case _ if account.balance.currency != amountToDebit.currency => None
      case _ => Some(account.copy(balance = account.balance - amountToDebit))
    }
  }

  private def credit(account: Account, amountToCredit: Money): Option[Account] = {
    Some(account.copy(balance = account.balance + amountToCredit))
  }

  private def executeTransaction(accountToDebit: Account, accountToCredit: Account, amount: Money) = {
    for {
      debited <- debit(accountToDebit, amount)
      credited <- credit(accountToCredit, amount)
    } yield (debited, credited)
  }

  private def validateTransaction(debitId: AccountId, creditId: AccountId, amount: Money) = {
    val accounts = wallet.accounts
    for {
      accToDebit <- accounts.find(_.accountId == debitId)
      accToCredit <- accounts.find(_.accountId == creditId)
      if accToDebit.balance.amount >= amount.amount
    } yield (accToDebit, accToCredit)
  }
}
