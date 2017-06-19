package logic

import org.specs2.mutable._

class ApartmentInfoParserSpec extends Specification {
  "getArea" should {
    "parse single values correctly" in {
      ApartmentInfoParser.getArea("1 m2") mustEqual Some(1)
      ApartmentInfoParser.getArea("18 m2") mustEqual Some(18)
      ApartmentInfoParser.getArea("1 005 m2") mustEqual Some(1005)
    }
    "parse multiple values correctly" in {
      ApartmentInfoParser.getArea("44 m2 44 m2 55 m2") mustEqual Some(44)
      ApartmentInfoParser.getArea("45 m2 45 m2") mustEqual Some(45)
      ApartmentInfoParser.getArea("160 m2 160 m2") mustEqual Some(160)
    }
    "return none for incorrect values" in {
      ApartmentInfoParser.getArea("m2") mustEqual None
      ApartmentInfoParser.getArea("what the") mustEqual None
    }
  }

  "getPriceInEur" should {
    "parse single eur value" in {
      ApartmentInfoParser.getPriceInEur(None)("430,00 €") mustEqual Some(BigDecimal(430))
      ApartmentInfoParser.getPriceInEur(None)("1 400,00 €") mustEqual Some(BigDecimal(1400))
    }

    "parse multiple eur value" in {
      ApartmentInfoParser.getPriceInEur(None)("450,00 + 50,00 €") mustEqual Some(BigDecimal(500))
    }

    "parse single czk value" in {
      ApartmentInfoParser.getPriceInEur(Some(BigDecimal(20)))("8 000,00 CZK") mustEqual Some(BigDecimal(400))
    }

    "return none for czk without conversion rate" in {
      ApartmentInfoParser.getPriceInEur(None)("8 000,00 CZK") mustEqual None
    }

    "parse multiple czk value" in {
      ApartmentInfoParser.getPriceInEur(Some(BigDecimal(20)))("10 000,00 + 2 900,00 CZK") mustEqual Some(BigDecimal(645))
    }

    "return none if can't parse value" in {
      ApartmentInfoParser.getPriceInEur(None)("cena dohodou") mustEqual None
    }
  }
}
