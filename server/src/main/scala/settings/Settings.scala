package settings

import com.typesafe.config.ConfigFactory

trait Settings {

  lazy val conf = ConfigFactory.load()

  lazy val elastic = conf.getConfig("elastic")
  lazy val elasticUrl = elastic.getString("url")
  lazy val elasticPort = elastic.getInt("port")
}
