package model

import java.time.LocalDateTime
import java.util.UUID

case class Transaction(
                        transactionId: TransactionId,
                        debit: Account,
                        credit: Account,
                        amount: Money,
                        created: LocalDateTime
                      )

final case class TransactionId(id: String) extends AnyVal {
  override def toString: String = id
}
object TransactionId {
  def newTransactionId: TransactionId = new TransactionId(UUID.randomUUID().toString)
}