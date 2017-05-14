package model

import play.api.libs.json.Json

case class Location(lat: Double, lng: Double)

object Location {
  implicit val locationReads = Json.reads[Location]
}
