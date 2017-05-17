
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.sksamuel.elastic4s.ElasticsearchClientUri
import com.sksamuel.elastic4s.http.HttpClient
import common.{Logging, Settings}
import logic._

import scala.util.{Failure, Success}

object Main extends App with Settings with Logging {

  implicit val system = ActorSystem("system")
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  val client = HttpClient(ElasticsearchClientUri(elasticHost.url, elasticHost.port))
  val elasticClient = new ElasticSearch(client)
  val locationResolver = new LocationResolver(googleApi.maxRequestPerDay, LocationResolver.getAddress)
  val topReality = new TopReality(
    () => 1, // TopRealityParser.getNumberOfPages
    TopRealityParser.getApartmentsFromDocument)
  val parseCycle = new ParseCycle(elasticClient, topReality, locationResolver)

  def shutDown = {
    client.close()
    system.terminate()
    ()
  }

  log.info("Starting parse cycle")
  parseCycle.run.andThen {
    case Success(_) =>
      log.info("Parse cycle finished succesfully")
      shutDown
    case Failure(f) =>
      log.error("Parse cycle failed", f)
      shutDown
  }
}
