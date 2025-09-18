package id.co.awan.walle.service.core

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient
import co.elastic.clients.transport.ElasticsearchTransportConfig
import org.apache.hc.core5.ssl.SSLContexts
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import java.security.cert.X509Certificate
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter


abstract class ElasticCoreAbstract(
) {

    companion object {

        data class HttpRequestLog(
            val method: String,
            val baseUrl: String,
            val path: String,
            val query: String?,
            val headers: Map<String, String>,
            val body: String?
        )

        data class HttpResponseLog(
            val statusCode: Int,
            val statusText: String,
            val headers: Map<String, String>,
            val body: String?
        )

        data class HttpLog(
            val request: HttpRequestLog,
            val response: HttpResponseLog,
            val totalTime: Long,
            val createdAt: String,
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

    private fun getIndexSuffixDate(): String {
        val dtf = DateTimeFormatter.ofPattern("ddMMyyyy")
        val now = Instant.now()
        val nowAtZone = now.atZone(ZoneId.systemDefault())
        return nowAtZone.format(dtf)
    }

    private fun getCreatedAt(): String {
        val dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss Z")
        val now = Instant.now()
        val nowAtZone = now.atZone(ZoneId.systemDefault())
        return nowAtZone.format(dtf)
    }


    fun submitLog(
        req: HttpRequestLog,
        res: HttpResponseLog,
        totalTime: Long
    ) {

        val esClientAsync: ElasticsearchAsyncClient =
            ElasticsearchAsyncClient.of { b: ElasticsearchTransportConfig.Builder ->
                b
                    .host(elasticHost)
//                    .usernameAndPassword(elasticUser, elasticPassword)
                    .apiKey(elasticApiKey)
                    .sslContext(
                        SSLContexts.custom()
                            .loadTrustMaterial(null) { _: Array<X509Certificate?>?, _: String? -> true }
                            .build())
            }

        esClientAsync.index {
            it
                .index("walle-${getIndexSuffixDate()}")
                .document(HttpLog(req, res, totalTime, getCreatedAt()))
        }.whenComplete { response, exception ->
            if (exception != null) {
                logger.error("Failed to index", exception)
            } else {
                logger.info("Indexed with version " + response.version())
            }
        }

    }

}