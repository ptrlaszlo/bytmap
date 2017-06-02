package logic

import akka.stream.ThrottleMode
import akka.stream.scaladsl.Source
import common.Logging
import model.TopRealityApartment
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

class TopReality(
  pageCount: => Int,
  pageNumToDocument: Int => Future[Document],
  apartmentParser: Document => List[TopRealityApartment]) extends Logging {

  def crawlApartments(implicit ec: ExecutionContext) = {
    val pages = pageCount
    log.info(s"Crawling $pages pages from topreality")
    Source(1 to pages)
      .throttle(1, 2 seconds, 1, ThrottleMode.Shaping)
      .mapAsyncUnordered(4) { pageNumber =>
        log.info(s"Getting topreality data for page $pageNumber")
        pageNumToDocument(pageNumber).map(apartmentParser)
      }
      .mapConcat(identity)
  }
}

object TopRealityParser {

  private val base = "https://www.topreality.sk"

  def generateUrl(pageNumber: Int) =
    s"$base/vyhladavanie-nehnutelnosti-$pageNumber.html?type[0]=101&type[1]=108&type[2]=102&type[3]=103&type[4]=104&type[5]=105&type[6]=106&type[7]=109&type[8]=110&type[9]=107&form=3&n_search=search&searchType=string&sort=date_desc"

  def readDataFromPage(pageNumber: Int)(implicit ec: ExecutionContext): Future[Document] = Future {
    val url = generateUrl(pageNumber)
    Jsoup.connect(url).get
  }

  def getApartmentsFromDocument(doc: Document) = {
    doc.select("div.estate").iterator.asScala.toList.flatMap(parseApartment)
  }

  def parseApartment(apartment: Element): Option[TopRealityApartment] = {
    Try {
      val a = apartment.select("h2 a")
      val imgSrc = apartment.select("img").attr("src")
      val image = if (imgSrc.startsWith("http")) imgSrc else base + imgSrc
      TopRealityApartment(
        link = a.attr("href"),
        title = a.attr("title"),
        area = apartment.select("span.areas strong").text,
        address = apartment.select("span.locality").text,
        price = apartment.select("span.price").text,
        date = apartment.select("span.date").text,
        image = image
      )
    }.toOption
  }

  val adsPerPage = 15

  val getNumberOfPages: Int =
    Try {
      val documentForPageNumber = Jsoup.connect(s"$base/vyhladavanie-nehnutelnosti-1.html").get
      val numberOfAds = documentForPageNumber.select("p.count strong").text.replace(" ", "").toInt
      val pages = numberOfAds * 1.0 / adsPerPage
      if (pages % 1 == 0) pages.toInt else pages.toInt + 1
    } match {
      case Success(p) => p
      case Failure(e) => throw new Exception("Couldn't parse page count from topreality", e)
    }
}
