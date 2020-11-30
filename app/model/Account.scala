package model


import java.time.LocalDateTime
import java.util.UUID

import model.wallets.{GanbaruClientId, WalletId}

import scala.util.{Failure, Try}


case class Account(
                    walletId: WalletId,
                    ganbaruClientId: GanbaruClientId,
                    accountId: AccountId,
                    cuit: String,
                    accountType: AccountType.AccountType,
                    balance: Money,
                    dateOpened: LocalDateTime
                    //TODO status: Open, Closed, Etc
                  )

object AccountType extends Enumeration {
  type AccountType = Value
  val Spot, Margin, Futures, P2P, Pool = Value
}

final case class AccountId(id: String) extends AnyVal {
  override def toString: String = id
}
object AccountId {
  def newAccountId: AccountId = new AccountId(UUID.randomUUID().toString)
}

