package common

import com.typesafe.config.ConfigFactory

trait Settings {

  val conf = ConfigFactory.load()

  object elasticHost {
    private val elastic = conf.getConfig("elastic")
    val url = elastic.getString("url")
    val port = elastic.getInt("port")
  }

  object googleApi {
    private val api = conf.getConfig("google.api")
    val maxRequestPerDay = api.getInt("maxRequestPerDay")
    val key = api.getString("key")
    val url = api.getString("url")
  }
}
