
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import common.{Logging, Settings}
import logic._

import scala.util.{Failure, Success}

object Main extends App with Settings with Logging {
  implicit val system = ActorSystem("system")
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  val elasticClient = ElasticClient.getAwsClient
  val elasticLogic = new ElasticSearch(elasticClient)
  val locationResolver = new LocationResolver(GoogleApi.maxRequestPerDay, LocationResolver.getAddress)
  val topReality = new TopReality(
    Crawler.pagesToCrawl, //TopRealityParser.getNumberOfPages,
    TopRealityParser.readDataFromPage,
    TopRealityParser.getApartmentsFromDocument)
  val parseCycle = new ParseCycle(elasticLogic, topReality, locationResolver, CurrencyConversion.getCzkPrice)

  def shutDown = {
    elasticClient.close()
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
      sys.exit(1)
  }
}
