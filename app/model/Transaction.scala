package model

import java.time.LocalDateTime
import java.util.UUID

case class Transaction(
                        transactionId: TransactionId,
                        to: Account,
                        from: Account,
                        amount: Money,
                        created: LocalDateTime
                      )

case class TransactionId(transactionId: String)
object TransactionId {
  def newTransactionId: TransactionId = new TransactionId(UUID.randomUUID().toString)
}