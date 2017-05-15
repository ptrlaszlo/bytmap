package logic

import akka.stream.scaladsl.Sink
import org.elasticsearch.common.geo.GeoPoint

object LocationMap {

  val addressMapFromLocations = Sink.fold[Map[String, GeoPoint], (String, GeoPoint)](Map.empty){
    case (locationMap, (address, location)) =>
      locationMap + (address -> location)
    }
}
