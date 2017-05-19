//package logic
//
//import akka.actor.ActorSystem
//import akka.stream.ActorMaterializer
//import akka.testkit.TestKit
//import com.sksamuel.elastic4s.http.ElasticDsl._
//import com.sksamuel.elastic4s.ElasticsearchClientUri
//import com.sksamuel.elastic4s.http.HttpClient
//import logic._
//import org.jsoup.Jsoup
//import org.specs2.mutable._
//
//import scala.concurrent.{Await, Future}
//import scala.concurrent.duration._
//import java.io.File
//
//import common.Logging
//import model.TopRealityApartment
//import org.specs2.specification.{AfterAll, Scope}
//
//class ElasticTestClient(indexName: String, client: HttpClient) extends ElasticSearch(client) {
//  override val indexRent = indexName
//}
//
//trait ParseCycleSpec extends Scope with BeforeAfter with Logging {
//  private val httpClient = HttpClient(ElasticsearchClientUri("localhost", 9200))
//
//  val indexName = "test_rents"
//  val elasticClient = new ElasticTestClient(indexName, httpClient)
//
//  def before = {
//    log.info(s"Deleting index $indexName")
//    httpClient.execute {
//      deleteIndex(indexName)
//    }
//  }
//
//  def after = {
//    httpClient.close
//  }
//}
//
//class ParseCycleSpec extends TestKit(ActorSystem("MySpec")) with SpecLike with Logging with AfterAll {
//
//  def afterAll() = {
//    TestKit.shutdownActorSystem(system)
//  }
//
//  "ASD" should {
//    "XX" in new CustomScope {
//      implicit val materializer = ActorMaterializer()
//      val testFile = new File("src/test/resources/testpage.html")
//      val locationResolver = new LocationResolver(1, _ => Future.successful(None))
//      val topReality = new TopReality(
//        1,
//        _ => Future.successful(Jsoup.parse(testFile, "UTF-8")),
//        TopRealityParser.getApartmentsFromDocument)
//      val parseCycle = new ParseCycle(elasticClient, topReality, locationResolver)
//
//      Await.result(parseCycle.run, 5 seconds)
//      val apFuture = elasticClient.getWithoutLocation.runFold(List.empty[TopRealityApartment]) { case (l, i) => i :: l  }
//      val result = Await.result(apFuture, 5 seconds)
//
//      result.size mustEqual 15
//    }
//  }
//}
