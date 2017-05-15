
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.sksamuel.elastic4s.ElasticsearchClientUri
import com.sksamuel.elastic4s.http.HttpClient
import logging.Logging
import logic.{ElasticSearch, LocationMap, LocationResolver, TopReality}
import settings.Settings

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
//    locationMap <- elasticClient.getAddressWithLocation.runWith(LocationMap.addressMapFromLocations)

    apartmentsSaved = Promise[Unit]
    _ = TopReality.crawlApartments.runWith(elasticClient.upsertApartment(apartmentsSaved))
    _ <- apartmentsSaved.future

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
