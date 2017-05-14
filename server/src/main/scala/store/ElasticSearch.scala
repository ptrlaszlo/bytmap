package store

import akka.actor.ActorSystem
import akka.stream.scaladsl.Sink
import com.sksamuel.elastic4s.bulk.BulkCompatibleDefinition
import com.sksamuel.elastic4s.http.HttpClient
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.mappings._
import com.sksamuel.elastic4s.streams.RequestBuilder
import com.sksamuel.elastic4s.streams.ReactiveElastic._
import model.TopRealityApartment

import scala.concurrent.{ExecutionContext, Future, Promise}

class ElasticSearch(client: HttpClient) {

  private val indexRent = "rents"
  private val typeApartment = "apartment"

  def initIndex(implicit ec: ExecutionContext): Future[Unit] = {
    client.execute(
      createIndex(indexRent).mappings(
        MappingDefinition(typeApartment)
          .as(
            geopointField("location")
          )
      )
    )
    .map(_ => ())
    .recover {
      case f if f.getMessage.contains("index_already_exists_exception") => ()
    }
  }

  private def indexOrUpdate(apartment: TopRealityApartment) = {
    println("INSERT OR UPDATE")
    update(apartment.link).in(indexRent, typeApartment).docAsUpsert(
      "topreality.link" -> apartment.link,
      "topreality.title" -> apartment.title,
      "topreality.area" -> apartment.area,
      "topreality.address" -> apartment.address,
      "topreality.price" -> apartment.price,
      "topreality.date" -> apartment.date
    )
  }

  def upsertApartmentSink(completeOnFinish: Promise[Unit])(implicit as: ActorSystem) = {
    implicit val apartmentRequestBuilder = new RequestBuilder[TopRealityApartment] {
      def request(t: TopRealityApartment): BulkCompatibleDefinition = indexOrUpdate(t)
    }

    val completeFn: () => Unit = () => completeOnFinish.success(())

    val apartmentSubscriber = client.subscriber[TopRealityApartment](20, 1, completionFn = completeFn)
    Sink.fromSubscriber(apartmentSubscriber)
  }
}
