package logic

import common.Logging

import scala.util.Try

object ApartmentInfoParser extends Logging {

  private val areaRegex = "^([0-9]{1,4})m2.*$".r

  def getArea(str: String): Option[Int] = str.replace(" ","") match {
    case areaRegex(area) => Some(area.toInt)
    case other =>
      log.warn(s"Couldn't parse area $other")
      None
  }

  private val eurRegex = "(.*)€".r
  private val czkRegex = "(.*)CZK".r

  def getPriceInEur(eurInCzk: Option[BigDecimal])(str: String): Option[BigDecimal] = {
    def sumPrices(priceStr: String): Option[BigDecimal] =
      Try(priceStr.replace(" ", "").replace(",", ".").split('+').map(BigDecimal(_)).sum).toOption

    str match {
      case eurRegex(values) => sumPrices(values)
      case czkRegex(values) => sumPrices(values).flatMap(price => eurInCzk.map(rate => price / rate))
      case other =>
        log.warn(s"Couldn't parse price $other")
        None
    }
  }
}



