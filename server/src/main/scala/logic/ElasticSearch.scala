package logic

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import com.sksamuel.elastic4s.bulk.BulkCompatibleDefinition
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.HttpClient
import com.sksamuel.elastic4s.mappings._
import com.sksamuel.elastic4s.streams.ReactiveElastic._
import com.sksamuel.elastic4s.streams.RequestBuilder
import model.{Location, TopRealityApartment}
import org.elasticsearch.common.geo.GeoPoint

import scala.concurrent.{ExecutionContext, Future, Promise}

class ElasticSearch(client: HttpClient) {

  private val indexRent = "rents"
  private val typeApartment = "apartment"
  private val fieldLocation = "location"

  def initIndex(implicit ec: ExecutionContext): Future[Unit] = {
    client.execute(
      createIndex(indexRent).mappings(
        MappingDefinition(typeApartment)
          .as(
            geopointField(fieldLocation)
          )
      )
    )
    .map(_ => ())
    .recover {
      case f if f.getMessage.contains("index_already_exists_exception") => ()
    }
  }

  private def indexOrUpdate(id: String, values: Map[String, Any]) = {
    update(id).in(indexRent, typeApartment).docAsUpsert(values)
  }

  def upsertApartment(completeOnFinish: Promise[Unit])(implicit as: ActorSystem) = {
    implicit val apartmentRequestBuilder = new RequestBuilder[TopRealityApartment] {
      def request(t: TopRealityApartment): BulkCompatibleDefinition = indexOrUpdate(t.link, t.toMap)
    }

    val completeFn: () => Unit = () => completeOnFinish.success(())

    val apartmentSubscriber = client.subscriber[TopRealityApartment](20, 1, completionFn = completeFn)
    Sink.fromSubscriber(apartmentSubscriber)
  }

  def upsertLocation(completeOnFinish: Promise[Unit])(implicit as: ActorSystem) = {
    implicit val apartmentRequestBuilder = new RequestBuilder[(TopRealityApartment, Location)] {
      def request(t: (TopRealityApartment, Location)): BulkCompatibleDefinition = {
        val values = Map(fieldLocation -> new GeoPoint(t._2.lat, t._2.lng))
        indexOrUpdate(t._1.link, values)
      }
    }

    val completeFn: () => Unit = () => completeOnFinish.success(())

    val locationSubscriber = client.subscriber[(TopRealityApartment, Location)](20, 1, completionFn = completeFn)
    Sink.fromSubscriber(locationSubscriber)
  }

  def getWithoutLocation(implicit as: ActorSystem) = {
    Source.fromPublisher(client.publisher {
      search(indexRent).types(typeApartment).query(
        boolQuery().not(existsQuery("location"))
      ).scroll("10m")
    })
    .map(hit => TopRealityApartment.fromMap(hit.sourceAsMap))
    .collect {
      case Some(v) => v
    }
  }
}
