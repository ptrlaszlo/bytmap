package logic

object ApartmentInfoParser {

  private val areaRegex = "^([0-9]{1,4}) m2.*$".r

  def getArea(str: String): Option[Int] = str match {
    case areaRegex(area) => Some(area.toInt)
    case _ => None
  }
}



