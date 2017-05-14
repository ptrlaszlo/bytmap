package model

final case class TopRealityApartment(
  link: String,
  title: String,
  area: String,
  address: String,
  price: String,
  date: String) {
  lazy val toMap = Map[String, String](
      "topreality.link" -> link,
      "topreality.title" -> title,
      "topreality.area" -> area,
      "topreality.address" -> address,
      "topreality.price" -> price,
      "topreality.date" -> date
  )
}

object TopRealityApartment {
  def fromMap(valueMap: Map[String, AnyRef]): Option[TopRealityApartment] = for {
    link <- valueMap.get("topreality.link")
    title <- valueMap.get("topreality.title")
    area <- valueMap.get("topreality.area")
    address <- valueMap.get("topreality.address")
    price <- valueMap.get("topreality.price")
    date <- valueMap.get("topreality.date")
  } yield TopRealityApartment(
    link = link.toString,
    title = title.toString,
    area = area.toString,
    address = address.toString,
    price = price.toString,
    date = date.toString
  )
}