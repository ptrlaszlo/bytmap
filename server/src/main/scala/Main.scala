
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.sksamuel.elastic4s.ElasticsearchClientUri
import com.sksamuel.elastic4s.http.HttpClient
import logging.Log
import settings.Settings
import source.TopReality
import store.ElasticSearch

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Promise

object Main extends App with Settings with Log {

  implicit val system = ActorSystem("system")
  implicit val materializer = ActorMaterializer()

  val client = HttpClient(ElasticsearchClientUri(elasticUrl, elasticPort))
  val elasticClient = new ElasticSearch(client)

  def shutDown = {
    client.close()
    system.terminate()
    ()
  }

  val logic = for {
    _ <- elasticClient.initIndex
    elasticUpdateFinished = Promise[Unit]
    _ = TopReality.crawlApartments.runWith(elasticClient.upsertApartmentSink(elasticUpdateFinished))
    _ <- elasticUpdateFinished.future.map(_ => shutDown)
  } yield ()

  logic.recover {
    case t =>
      shutDown
      throw(t)
  }
}
