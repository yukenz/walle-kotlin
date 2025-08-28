package id.co.awan.walle.service

import id.co.awan.walle.config.RestConfig
import id.co.awan.walle.config.RestTemplateTestConfig
import id.co.awan.walle.service.thirdparty.TelegramService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
    classes = [
        RestTemplateTestConfig::class,
        RestConfig::class,
        TelegramService::class
    ]
)
class TelegramServiceTest {

    @Autowired
    private lateinit var service: TelegramService


    @Test
    fun getMe() {
        service.getMe()
    }

    @Test
    fun testGetUpdates() {
        service.getUpdates()
    }

    @Test
    fun testSendMessage() {
        service.sendMessage("mavenizm", "Hello World")
    }

    @Test
    fun testFindByUsername() {
        val chatId = service.getChatIdByUsername("mavenizm")
        print("chatId : $chatId")
    }
}