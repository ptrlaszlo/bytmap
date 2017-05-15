package common

import com.typesafe.config.ConfigFactory

trait Settings {

  lazy val conf = ConfigFactory.load()

  object elasticHost {
    private lazy val elastic = conf.getConfig("elastic")
    lazy val url = elastic.getString("url")
    lazy val port = elastic.getInt("port")
  }

  object googleApi {
    private lazy val api = conf.getConfig("google.api")
    lazy val maxRequestPerDay = api.getInt("maxRequestPerDay")
    lazy val key = api.getString("key")
    lazy val url = api.getString("url")
  }
}
