package logic

import akka.actor.ActorSystem
import akka.stream.Materializer

import scala.concurrent.{Future, Promise}

class ParseCycle(
  elasticClient: ElasticSearch,
  topReality: TopReality,
  locationResolver: LocationResolver,
  getCzkRate: => Future[Option[BigDecimal]])(implicit actorSystem: ActorSystem, mat: Materializer) {

  implicit val ec = actorSystem.dispatcher

  def run = for {
    _ <- elasticClient.initIndex
    // TODO do we need location cache?: locationMap <- elasticClient.getAddressWithLocation.runWith(LocationMap.addressMapFromLocations)

    czkRate <- getCzkRate

    // Crawling and saving apartments
    apartmentsSaved = Promise[Unit]
    _ = topReality.crawlApartments.runWith(elasticClient.upsertApartment(czkRate, apartmentsSaved))
    _ <- apartmentsSaved.future

    // Removing old ones
    _ <- elasticClient.removeNotModifiedToday

    // Getting loaction (lat, lon) based on address
    locationsSaved = Promise[Unit]
    _ = elasticClient.getWithoutLocation.via(locationResolver.getAddressFlow).runWith(elasticClient.upsertLocation(locationsSaved))
    _ <- locationsSaved.future
  } yield ()
}
