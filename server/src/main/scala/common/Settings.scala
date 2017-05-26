package common

import com.typesafe.config.ConfigFactory

trait Settings {

  val conf = ConfigFactory.load()

  object ElasticLocal {
    private val elastic = conf.getConfig("elastic-local")
    val url = elastic.getString("url")
    val port = elastic.getInt("port")
  }

  object AWS {
    private val aws = conf.getConfig("aws")
    val accessKey = aws.getString("accessKey")
    val secretKey = aws.getString("secretKey")
    val region = aws.getString("region")
    val elasticUrl = aws.getString("elastic.url")
    val elasticPort = aws.getInt("elastic.port")
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
