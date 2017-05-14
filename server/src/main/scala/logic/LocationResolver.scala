package logic

import akka.stream.ThrottleMode
import akka.stream.scaladsl.{Flow, Source}
import logging.Logging
import model.{Location, TopRealityApartment}
import play.api.libs.json.Json
import settings.Settings

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scalaj.http.{Http, HttpResponse}

object LocationResolver extends Settings with Logging {
  private def getAddress(apartment: TopRealityApartment)(implicit ec: ExecutionContext): Future[Option[(TopRealityApartment, Location)]] =
    Future {
      val response: HttpResponse[String] = Http(googleApi.url)
        .param("address", apartment.address)
        .param("key", googleApi.key)
        .asString
      // TODO Add preference to slovakia
      // TODO log errors

      if (response.code == 200) {
        (Json.parse(response.body) \\ "location").flatMap(_.asOpt[Location]).headOption.map((apartment, _))
      } else {
        log.warn(s"Couldn't get location for $apartment: ${response.body}")
        None
      }
    }

  val requestLimit = Source(1 to googleApi.maxRequestPerDay)

  def getAddressFlow(implicit ec: ExecutionContext) = Flow[TopRealityApartment]
    .zip(requestLimit)
    .mapAsync(2)(i => getAddress(i._1))
    .throttle(45, 1 seconds, 1, ThrottleMode.Shaping)
    .collect { case Some(v) => v}
}
