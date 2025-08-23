package id.co.awan.walle


import id.co.awan.walle.config.RestConfig
import id.co.awan.walle.service.CoinGeckoService
import org.junit.jupiter.api.Assertions.assertNotNull
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
        RestTemplateTestConfiguration::class,
        RestConfig::class,
        CoinGeckoService::class
    ]
)
//@TestPropertySource("classpath:application.yml")
class CoinGeckoServiceTest {

    private val log = LoggerFactory.getLogger(this::class.java)

    @Autowired
    private lateinit var service: CoinGeckoService

    @Test
    fun contextLoads() {
        assertNotNull(service)
    }

    @Test
    fun testRequest() {
        service.ping()
        val coinPrice = service.coinPrice("ethereum", "IDR", "0xdac17f958d2ee523a2206206994597c13d831ec7", 16)
        assert(coinPrice > BigDecimal.TWO)
    }

}
