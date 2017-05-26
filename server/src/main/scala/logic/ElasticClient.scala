package logic

import java.time.{LocalDateTime, ZoneId}

import com.sksamuel.elastic4s.ElasticsearchClientUri
import com.sksamuel.elastic4s.http.HttpClient
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback
import vc.inreach.aws.request.{AWSSigner, AWSSigningRequestInterceptor}
import com.amazonaws.auth._
import com.amazonaws.internal.StaticCredentialsProvider
import common.{Logging, Settings}

object ElasticClient extends Settings with Logging {

  def getLocalClient: HttpClient = {
    log.info("Using elastic search client on localhost")
    HttpClient(ElasticsearchClientUri(ElasticLocal.url, ElasticLocal.port))
  }

  def getAwsClient: HttpClient = {
    class AWSSignerInteceptor extends HttpClientConfigCallback {
      val credentials: AWSCredentials = new BasicAWSCredentials(AWS.accessKey, AWS.secretKey)
      val awsCredentialsProvider: AWSCredentialsProvider = new StaticCredentialsProvider(credentials)
      val signer = new AWSSigner(awsCredentialsProvider, AWS.region, "es", () => LocalDateTime.now(ZoneId.of("UTC")))

      override def customizeHttpClient(httpClientBuilder: HttpAsyncClientBuilder): HttpAsyncClientBuilder = {
        httpClientBuilder.addInterceptorLast(new AWSSigningRequestInterceptor(signer))
      }
    }

    val hosts = ElasticsearchClientUri(AWS.elasticUrl, AWS.elasticPort).hosts.map { case (host, port) => new HttpHost(host, port, "http") }
    val restClient = RestClient.builder(hosts: _*).setHttpClientConfigCallback(new AWSSignerInteceptor).build()
    log.info("Using elastic search client on AWS")
    HttpClient.fromRestClient(restClient)
  }
}
