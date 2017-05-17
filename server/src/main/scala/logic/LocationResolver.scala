package logic

import akka.stream.ThrottleMode
import akka.stream.scaladsl.{Flow, Source}
import common.{Logging, Settings}
import model.{Location, TopRealityApartment}
import play.api.libs.json.Json

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scalaj.http.{Http, HttpResponse}

class LocationResolver(
  maxRequest: Int,
  fromAddress: TopRealityApartment => Future[Option[(TopRealityApartment, Location)]]) {

  val requestLimit = Source(1 to maxRequest)

  def getAddressFlow(implicit ec: ExecutionContext) = Flow[TopRealityApartment]
    .zip(requestLimit)
    .mapAsync(2)(i => fromAddress(i._1))
    .throttle(45, 1 seconds, 1, ThrottleMode.Shaping)
    .collect { case Some(v) => v}
}

object LocationResolver extends Settings with Logging {

  def getAddress(implicit ec: ExecutionContext): TopRealityApartment => Future[Option[(TopRealityApartment, Location)]] =
    apartment =>
      Future {
        log.debug(s"Resolving address for ${apartment.address}")
        val response: HttpResponse[String] = Http(googleApi.url)
          .param("address", apartment.address)
          .param("key", googleApi.key)
          .asString
        // TODO Add preference for slovakia

        if (response.code == 200) {
          (Json.parse(response.body) \\ "location").flatMap(_.asOpt[Location]).headOption.map((apartment, _))
        } else {
          log.warn(s"Couldn't get location for $apartment: ${response.body}")
          None
        }
      }

}
