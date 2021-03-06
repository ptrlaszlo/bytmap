package logic

import akka.stream.ThrottleMode
import akka.stream.scaladsl.{Flow, Source}
import common.{Logging, Settings}
import model.{Location, TopRealityApartment}
import play.api.libs.json.{JsString, Json}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scalaj.http.{Http, HttpResponse}

class LocationResolver(
  maxRequest: Int,
  fromAddress: TopRealityApartment => Future[Option[(TopRealityApartment, Location)]]) extends Logging {

  val requestLimit = Source(1 to maxRequest)

  def getAddressFlow(implicit ec: ExecutionContext) = Flow[TopRealityApartment]
    .zip(requestLimit)
    .mapAsyncUnordered(1){ i =>
      log.debug(s"Location request ${i._2}")
      fromAddress(i._1)
    }
    .throttle(45, 1 seconds, 1, ThrottleMode.Shaping)
    .collect { case Some(v) => v}
}

object LocationResolver extends Settings with Logging {

  def getAddress(implicit ec: ExecutionContext): TopRealityApartment => Future[Option[(TopRealityApartment, Location)]] =
    apartment =>
      Future {
        log.debug(s"Resolving address for ${apartment.address}")
        val response: HttpResponse[String] = Http(GoogleApi.url)
          .param("address", apartment.address)
          .param("key", GoogleApi.key)
          .asString
        // TODO Add preference for slovakia

        if (response.code == 200) {
          val parsedBody = Json.parse(response.body)
          (parsedBody \ "status").toOption match {
            case Some(JsString("OK")) => (parsedBody \\ "location").flatMap(_.asOpt[Location]).headOption.map((apartment, _))
            case Some(JsString("ZERO_RESULTS")) => None
            case Some(JsString("OVER_QUERY_LIMIT")) => throw new Exception(s"Google API query limit exceeded: $parsedBody")
            case _ => throw new Exception(s"Unexpected response from Google API: $parsedBody")
          }
        } else {
          log.warn(s"Couldn't get location for $apartment: Status code:${response.code} ${response.body}")
          None
        }
      }

}
