package id.co.awan.walle.service

import id.co.awan.walle.config.RestConfig
import id.co.awan.walle.config.RestTemplateTestConfig
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
    classes = [
        RestTemplateTestConfig::class,
        RestConfig::class,
        ERC20MiddlewareService::class
    ]
)
class ERC20MiddlewareServiceTest {

    private val log = LoggerFactory.getLogger(this::class.java)

    @Autowired
    private lateinit var service: ERC20MiddlewareService

    @Test
    fun totalSupply() {
        val totalSupply = service.totalSupply("anvil", "0x5FbDB2315678afecb367f032d93F642f64180aa3")
    }

    @Test
    fun allowance() {
        val totalSupply = service.allowance(
            "anvil",
            "0x5FbDB2315678afecb367f032d93F642f64180aa3",
            "0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266",
            "0x70997970C51812dc3A010C7d01b50e0d17dc79C8"
            )

    }

    @Test
    fun transfer() {
    }

    @Test
    fun transferFrom() {
    }

    @Test
    fun approve() {
    }

}