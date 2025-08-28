package id.co.awan.walle.service.thirdparty

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import id.co.awan.walle.service.core.TelegramCoreAbstract
import id.co.awan.walle.utils.LogUtils
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import org.springframework.web.server.ResponseStatusException

class TelegramService(
    private val restTemplate: RestTemplate
) : TelegramCoreAbstract(restTemplate) {

    fun sendMessage(username: String, message: String): ResponseEntity<JsonNode?> {

        val chatId = getChatIdByUsername(username) ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "01|Telegram account with $username not found"
        )

        val request = JsonNodeFactory.instance.objectNode().apply {
            put("chat_id", chatId)
            put("text", message)
        }

        val logToken: String = LogUtils.logHttpRequest(this.javaClass, "sendMessage", request)
        val responseEntity = super.post("sendMessage", null, request)
        responseEntity.body?.also {
            LogUtils.logHttpResponse(logToken, this.javaClass, it)
        }

        return responseEntity
    }

    fun getMe(): ResponseEntity<JsonNode?> {

        val responseEntity = super.post("getMe", null, null)
        responseEntity.body?.also {
            LogUtils.logHttpResponseWithoutToken("getMe", this.javaClass, it)
        }
        return responseEntity
    }

    fun getUpdates(): ResponseEntity<JsonNode?> {

        val responseEntity = super.post("getUpdates", null, null)
        responseEntity.body?.also {
            LogUtils.logHttpResponseWithoutToken("getUpdates", this.javaClass, it)
        }
        return responseEntity
    }

    fun getChatIdByUsername(username: String): Int? {
        getUpdates().body?.run {
            // Find JsonNode
            at("/result")
                .toMutableList().forEach {
                    val usernamePtr = it.at("/message/from/username")
                    if (usernamePtr.asText() contentEquals username) {
                        return it.at("/message/from/id").asInt()
                    }
                }
        }
        return null
    }
}