package model
/*

import java.time.LocalDateTime
import java.util.UUID

import akka.Done
import akka.actor.typed.{ActorRef, Behavior}
import akka.cluster.sharding.typed.scaladsl.{EntityContext, EntityTypeKey}
import akka.persistence.typed.scaladsl.ReplyEffect
import model.wallets.WalletCommands
import model.wallets.WalletCommands.{AddAccount, CreateWallet, GetWallet}
import play.api.libs.json._




final case class WalletAggregate(
                                  walletId: WalletId,
                                  cuit: String,
                                  accounts: List[Account] = List.empty,
                                  transactions: List[Transaction] = List.empty,
                                  dateCreated: LocalDateTime = LocalDateTime.now()
                                ) {

  def applyCommand(command: WalletCommands.Command): ReplyEffect[WalletEvent, WalletAggregate] =
    command match {
      case CreateWallet(walletId, walletConfirmation,replyTo) => ??? //onCreateWallet(create)
      case GetWallet(replyTo) => ??? //onGetWallet(replyTo)
      case AddAccount(cuit, balance, accountType, replyTo) => ??? //onAddAccount(cuit, balance, accountType, replyTo)
    }


  //def onCreateWallet(walletId: WalletId, walletConfirmation: WalletConfirmation, replyTo: ActorRef[Done])
  //def onGetWallet(replyTo: ActorRef[Done]): ReplyEffect[WalletEvent, WalletAggregate] = ???


  def applyEvent(event: WalletEvent): WalletAggregate =
    event match {
      case AccountAdded(account) => copy(accounts = accounts :+ account)
    }
}

object WalletAggregate {

  /** Initial State */
  val empty: WalletAggregate = WalletAggregate(WalletId(""), "")
  val typeKey: EntityTypeKey[WalletCommands.Command] = EntityTypeKey[WalletCommands.Command]("WalletAggregate")

  case class WalletConfirmation(
                                 gandaruClientId: GandaruClientId,
                                 cuit: String
                                 )

  def apply(entityConext: EntityContext[WalletCommands.Command]): Behavior[WalletCommands.Command] = ???

}

*/