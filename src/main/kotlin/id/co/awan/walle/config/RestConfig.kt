package id.co.awan.walle.config

import org.apache.hc.client5.http.config.ConnectionConfig
import org.apache.hc.client5.http.config.RequestConfig
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy
import org.apache.hc.client5.http.ssl.TlsSocketStrategy
import org.apache.hc.core5.pool.PoolConcurrencyPolicy
import org.apache.hc.core5.ssl.SSLContexts
import org.apache.hc.core5.ssl.TrustStrategy
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.ResponseErrorHandler
import org.springframework.web.client.RestTemplate
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit

@Configuration
class RestConfig {

    @Bean
    fun restTemplate(restTemplateBuilder: RestTemplateBuilder): RestTemplate {

        val connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
            .useSystemProperties()
            .setDefaultConnectionConfig(getConnectionConfig())
            .setMaxConnTotal(4)
            .setMaxConnPerRoute(2)
            .setTlsSocketStrategy(getTLSSocketStrategy())
            .setPoolConcurrencyPolicy(PoolConcurrencyPolicy.LAX)
            .build();

        val httpClient = HttpClients.custom()
            .disableCookieManagement()
            .setConnectionManager(connectionManager)
            .useSystemProperties()
            .setDefaultRequestConfig(
                RequestConfig.custom()
                    // LEASE TIMEOUT
                    .setConnectionRequestTimeout(5L, TimeUnit.SECONDS)
                    .build()
            )
            .build();

        val restTemplate = restTemplateBuilder.build()
        restTemplate.requestFactory = HttpComponentsClientHttpRequestFactory(httpClient)
        restTemplate.errorHandler = ResponseErrorHandler { _: ClientHttpResponse -> true }

        return restTemplate;
    }

    fun getConnectionConfig(): ConnectionConfig {
        return ConnectionConfig.custom()
            // Wait Full Established
            .setConnectTimeout(30L, TimeUnit.SECONDS)
            // Wait IO Operation / Send-Read Timeout
            .setSocketTimeout(30, TimeUnit.SECONDS)
            .build();

    }

    fun getTLSSocketStrategy(): TlsSocketStrategy {
        val acceptingTrustStrategy = TrustStrategy { _: Array<X509Certificate?>?, _: String? -> true }
        val sslContext = SSLContexts.custom()
            .loadTrustMaterial(null, acceptingTrustStrategy)
            .build();

        return DefaultClientTlsStrategy(sslContext)
    }

}