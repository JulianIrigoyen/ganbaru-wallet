package model


import java.time.LocalDateTime
import java.util.UUID

import model.wallets.{GandaruClientId, WalletId}

case class Account(
                  walletId: WalletId,
                  gandaruClientId: GandaruClientId,
                  accountId: AccountId,
                  cuit: String,
                  accountType: AccountType.AccountType,
                  balance: Money,
                  dateOpened: LocalDateTime
                  //TODO status: Open, Closed, Etc
                  )

object AccountType extends Enumeration {
  type AccountType = Value
  val Crypto, Fiat, Futuros = Value
}

final case class AccountId(id: String) extends AnyVal {
  override def toString: String = id
}
object AccountId {
  def newAccountId: AccountId = new AccountId(UUID.randomUUID().toString)
}

