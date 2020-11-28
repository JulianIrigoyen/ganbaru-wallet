package model

case class Money(amount: BigDecimal, currency: Money.Currency) {

  def +(that: Money): Money = {
    require(that.currency == currency)
    Money(amount + that.amount, currency)
  }

  def -(that: Money): Money = {
    require(that.currency == currency)
    Money(amount - that.amount, currency)
  }

}

object Money extends Enumeration {
  type Currency = Value
  val ARS, BRL, CLP, COP = Value

  def ars(amount: BigDecimal): Money = Money(amount, ARS)
  def brl(amount: BigDecimal): Money = Money(amount, BRL)
  def clp(amount: BigDecimal): Money = Money(amount, CLP)
  def cop(amount: BigDecimal): Money = Money(amount, COP)
}
