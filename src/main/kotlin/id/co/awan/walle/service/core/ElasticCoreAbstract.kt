package id.co.awan.walle.service.core

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient
import co.elastic.clients.transport.ElasticsearchTransportConfig
import org.apache.hc.core5.ssl.SSLContexts
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import java.security.cert.X509Certificate


abstract class ElasticCoreAbstract(
) {

    companion object {

        data class HttpRequestLog(
            val method: String,
            val baseUrl: String,
            val path: String,
            val query: String,
            val headers: Map<String, String>,
            val body: String
        )

        data class HttpResponseLog(
            val statusCode: Int,
            val statusText: String,
            val headers: Map<String, String>,
            val body: String
        )

        data class HttpLog(
            val request: HttpRequestLog,
            val response: HttpResponseLog,
            val totalTime: Long,
        )

        private val logger = LoggerFactory.getLogger(this::class.java)
    }


    @Value("\${es.host}")
    private lateinit var elasticHost: String

    @Value("\${es.username}")
    private lateinit var elasticUser: String

    @Value("\${es.password}")
    private lateinit var elasticPassword: String

    @Value("\${es.apikey}")
    private lateinit var elasticApiKey: String

    fun submitLog(
        req: HttpRequestLog,
        res: HttpResponseLog,
        totalTime: Long
    ) {

        val esClientAsync: ElasticsearchAsyncClient =
            ElasticsearchAsyncClient.of { b: ElasticsearchTransportConfig.Builder ->
                b
                    .host(elasticHost)
                    .usernameAndPassword(elasticUser, elasticPassword)
                    .sslContext(
                        SSLContexts.custom()
                            .loadTrustMaterial(null) { _: Array<X509Certificate?>?, _: String? -> true }
                            .build())
            }

        esClientAsync.index {
            it
                .index("walle-13092025")
                .document(HttpLog(req, res, totalTime))
        }.whenComplete { response, exception ->
            if (exception != null) {
                logger.error("Failed to index", exception)
            } else {
                logger.info("Indexed with version " + response.version())
            }
        }

    }

}