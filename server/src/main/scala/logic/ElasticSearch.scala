package logic

import akka.NotUsed
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
      def request(t: TopRealityApartment): BulkCompatibleDefinition = {
        val valueMap = t.toMap //TODO add updated time

        indexOrUpdate(t.link, valueMap)
      }
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

  def getAddressWithLocation(implicit as: ActorSystem): Source[(String, GeoPoint), NotUsed] = {
    Source.fromPublisher(client.publisher {
      search(indexRent).types(typeApartment).query(
        boolQuery().must(existsQuery(fieldLocation))
      ).scroll("1m")
    })
    .mapConcat { hit =>
      val source = hit.sourceAsMap
      (for {
        apartment <- TopRealityApartment.fromMap(source)
        location <- source.get(fieldLocation)
        locationMap = location.asInstanceOf[Map[String, Double]]
        lat <- locationMap.get("lat")
        lon <- locationMap.get("lon")
      } yield (apartment.address, new GeoPoint(lat, lon))).toList
    }
  }

  def getWithoutLocation(implicit as: ActorSystem): Source[TopRealityApartment, NotUsed] = {
    Source.fromPublisher(client.publisher {
      search(indexRent).types(typeApartment).query(
        boolQuery().not(existsQuery(fieldLocation))
      ).scroll("1m")
    })
    .mapConcat(hit => TopRealityApartment.fromMap(hit.sourceAsMap).toList)
  }
}
