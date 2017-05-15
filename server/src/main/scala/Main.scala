
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.sksamuel.elastic4s.ElasticsearchClientUri
import com.sksamuel.elastic4s.http.HttpClient
import common.{Logging, Settings}
import logic.{ElasticSearch, LocationMap, LocationResolver, TopReality}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Promise

object Main extends App with Settings with Logging {

  implicit val system = ActorSystem("system")
  implicit val materializer = ActorMaterializer()

  val client = HttpClient(ElasticsearchClientUri(elasticHost.url, elasticHost.port))
  val elasticClient = new ElasticSearch(client)

  def shutDown = {
    client.close()
    system.terminate()
    ()
  }

  val logic = for {
    _ <- elasticClient.initIndex
    // TODO do we need location cache?: locationMap <- elasticClient.getAddressWithLocation.runWith(LocationMap.addressMapFromLocations)

    // Crawling and saving apartments
    apartmentsSaved = Promise[Unit]
    _ = TopReality.crawlApartments.runWith(elasticClient.upsertApartment(apartmentsSaved))
    _ <- apartmentsSaved.future

    // Removing old ones
    _ <- elasticClient.removeNotModifiedToday

    // Getting loactio (lat, lon) based on address
    locationsSaved = Promise[Unit]
    _ = elasticClient.getWithoutLocation.via(LocationResolver.getAddressFlow).runWith(elasticClient.upsertLocation(locationsSaved))
    _ <- locationsSaved.future

    _ = shutDown
  } yield ()

  logic.recover {
    case t =>
      log.error(t.getMessage, t)
      shutDown
  }

}
