package model


import java.time.LocalDateTime
import java.util.UUID

case class Account(
                    accountId: AccountId,
                    cuit: String,
                    balance: Option[Money],
                    accountType: AccountType.AccountType,
                    dateOpened: LocalDateTime,
                    dateClosed: Option[LocalDateTime]
                  )

object AccountType extends Enumeration {
  type AccountType = Value
  val Crypto, Fiat, Futuros = Value
}

case class AccountId(accountId: String)
object AccountId {
  def newAccountId: AccountId = new AccountId(UUID.randomUUID().toString)
}

