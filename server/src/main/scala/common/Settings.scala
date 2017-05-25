package common

import com.typesafe.config.ConfigFactory

trait Settings {

  val conf = ConfigFactory.load()

  object ElasticHost {
    private val elastic = conf.getConfig("elastic")
    val url = elastic.getString("url")
    val port = elastic.getInt("port")
  }

  object GoogleApi {
    private val api = conf.getConfig("google.api")
    val maxRequestPerDay = api.getInt("maxRequestPerDay")
    val key = api.getString("key")
    val url = api.getString("url")
  }

  object Crawler {
    private val crawlerConf = conf.getConfig("crawler")
    val pagesToCrawl = crawlerConf.getInt("pagesToCrawl")
  }
}
