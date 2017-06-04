package logic

import common.Logging
import play.api.libs.json.{JsNumber, Json}

import scala.concurrent.{ExecutionContext, Future}
import scalaj.http.{Http, HttpResponse}

object CurrencyConversion extends Logging {

  def getCzkPrice(implicit ec: ExecutionContext): Future[Option[BigDecimal]] = Future {
    val response: HttpResponse[String] = Http("http://api.fixer.io/latest")
      .param("base", "EUR")
      .param("symbols", "CZK")
      .asString

    if (response.code == 200) {
      val parsedBody = Json.parse(response.body)
      (parsedBody \ "rates" \ "CZK").toOption match {
        case Some(JsNumber(rate)) => Some(rate)
        case other =>
          log.error(s"Unexpected response $other")
          None
      }
    } else {
      log.error(s"Couldn't get CZK conversion ${response.body}")
      None
    }
  }
}
