package logic

import akka.stream.ThrottleMode
import akka.stream.scaladsl.Source
import model.TopRealityApartment
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

object TopReality {

  def generateUrl(pageNumber: Int) =
    s"https://www.topreality.sk/vyhladavanie-nehnutelnosti-$pageNumber.html?type[0]=101&type[1]=108&type[2]=102&type[3]=103&type[4]=104&type[5]=105&type[6]=106&type[7]=109&type[8]=110&type[9]=107&form=3&n_search=search&searchType=string&sort=date_desc"

  val adsPerPage = 15

  def getNumberOfPages: Int = {
    Try {
      val documentForPageNumber = Jsoup.connect(generateUrl(1)).get
      val numberOfAds = documentForPageNumber.select("p.count strong").text.replace(" ", "").toInt
      val pages = numberOfAds * 1.0 / adsPerPage
      if (pages % 1 == 0) pages.toInt else pages.toInt + 1
    } match {
      case Success(p) => p
      case Failure(e) => throw new Exception("Couldn't parse page count from topreality", e)
    }
  }

  def parseApartment(estate: Element): Option[TopRealityApartment] = {
    Try {
      val a = estate.select("h2 a")
      TopRealityApartment(
        link = a.attr("href"),
        title = a.attr("title"),
        area = estate.select("span.areas strong").text,
        address = estate.select("span.locality").text,
        price = estate.select("span.price").text,
        date = estate.select("span.date").text
      )
    }.toOption
  }

  val crawlApartments = Source(1 to 1) // Source(1 to TopReality.getNumberOfPages)
    .throttle(1, 3 seconds, 1, ThrottleMode.Shaping)
    .map { pageNumber =>
      println("DOING REQUEST")
      val doc = Jsoup.connect(TopReality.generateUrl(pageNumber)).get
      doc.select("div.estate").iterator.asScala.toList.flatMap(TopReality.parseApartment)
    }
    .mapConcat(identity)
}
