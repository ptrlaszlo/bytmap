package logic

import model.TopRealityApartment
import org.jsoup.Jsoup
import org.specs2.mutable._

class TopRealitySpec extends Specification {
  val estateDiv = """<div class="estate"><div class="thumb"><a href="https://www.topreality.sk/prenajmem-maly-utulny-bytik-v-centre-s-vyhladom-na-jazera-r5940229.html" title="Prenajmem malý útulný bytík v centre s výhľadom na jazerá" target="_blank"><img src="/t/594/5940229-1-4.jpg?1495221492" alt="Prenajmem malý útulný bytík v centre s výhľadom na jazerá"/></a></div><h2><a href="https://www.topreality.sk/prenajmem-maly-utulny-bytik-v-centre-s-vyhladom-na-jazera-r5940229.html"  target="_blank" title="Prenajmem malý útulný bytík v centre s výhľadom na jazerá" class="">Prenajmem malý útulný bytík v centre s výhľadom na jazerá</a></h2><span class="areas">úžitková plocha <strong>41 m<sup>2</sup></strong></span><span class="locality">Lichnerova 39, Senec (Senec)</span><span class="price"><strong>400,00 &euro;</strong> </span><span class="priceArea">9,75 &euro;/m<sup>2</sup></span><span class="flags"><span class="pricePeriod">mesiac</span><span class="priceEnergy" title="S energiami">E</span></span><span class="date">19.5.2017</span><ul class="links"><li><strong><a href="https://www.topreality.sk/senec/byty/1-izbovy-byt/prenajom/" target="_blank" class="noclick">1 izbový byt Senec prenájom</a></strong></li><li><strong><a href="https://www.topreality.sk/senec/" target="_blank" class="noclick">Reality Senec</a></strong></li></ul><div class="tagsNotesButton"><a href="#" class="editTags" data-tagid="5940229" id="editTag5940229">Označiť</a> &nbsp;&nbsp;&nbsp;<a href="#" class="editNote" data-tagid="5940229" id="editNote5940229">Pridať poznámku</a></div></div>"""

  "TopRealityParser.getApartmentsFromDocument" should {
    "parse apartment from estate convertin HTML tags (<sup>) and HTML code (&euro;)" in {
      val result = TopRealityParser.getApartmentsFromDocument(Jsoup.parse(estateDiv))
      result.size mustEqual 1

      result(0) mustEqual TopRealityApartment(
        link = "https://www.topreality.sk/prenajmem-maly-utulny-bytik-v-centre-s-vyhladom-na-jazera-r5940229.html",
        title = "Prenajmem malý útulný bytík v centre s výhľadom na jazerá",
        area = "41 m2",
        address = "Lichnerova 39, Senec (Senec)",
        price = "400,00 €",
        date = "19.5.2017",
        image = "https://www.topreality.sk/t/594/5940229-1-4.jpg?1495221492"
      )
    }
  }
}
