package posting.http

import posting.http.HttpConfig.{HttpClientConfig, HttpServerConfig}

import scala.concurrent.duration.Duration

private[posting] final case class HttpConfig(server: HttpServerConfig, client: HttpClientConfig)

private[posting] object HttpConfig {
  final case class HttpServerConfig(
    host: String,
    port: Int,
    idleTimeout: Duration,
    responseHeaderTimeout: Duration,
    blockingPoolSize: Int,
  )
  final case class HttpClientConfig(
    connectionTimeout: Duration,
    requestTimeout: Duration,
    idleTimeout: Duration,
    blockingPoolSize: Int,
  )
}
