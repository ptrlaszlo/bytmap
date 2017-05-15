package common

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneId}

object Time {
  private val zone = ZoneId.of("CET")

  def getCurrentTime: LocalDateTime = LocalDateTime.now(zone)

  def getCurrentDateStr = getCurrentTime.format(dateFormatter)

  val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
}
