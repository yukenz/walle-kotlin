package id.co.awan.walle.service

import id.co.awan.walle.config.RestTemplateTestConfig
import id.co.awan.walle.config.RestConfig
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.math.BigDecimal

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
    classes = [
        RestTemplateTestConfig::class,
        RestConfig::class,
        CoinGeckoService::class
    ]
)
class CoinGeckoServiceTest {

    private val log = LoggerFactory.getLogger(this::class.java)

    @Autowired
    private lateinit var service: CoinGeckoService

    @Test
    fun contextLoads() {
        Assertions.assertNotNull(service)
    }

    @Test
    fun testRequest() {
        service.ping()
        val coinPrice = service.coinPrice("ethereum", "IDR", "0xdac17f958d2ee523a2206206994597c13d831ec7", 16)
        assert(coinPrice > BigDecimal.TWO)
    }

}