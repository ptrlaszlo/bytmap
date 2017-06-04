package logic

import org.specs2.mutable._

class ApartmentInfoParserSpec extends Specification {
  "getArea" should {
    "parse single values correctly" in {
      ApartmentInfoParser.getArea("1 m2") mustEqual Some(1)
      ApartmentInfoParser.getArea("18 m2") mustEqual Some(18)
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
}