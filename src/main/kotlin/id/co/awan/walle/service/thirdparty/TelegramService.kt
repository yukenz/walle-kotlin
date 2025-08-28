package id.co.awan.walle.service.thirdparty

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import id.co.awan.walle.service.core.TelegramCoreAbstract
import id.co.awan.walle.utils.LogUtils
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate

class TelegramService(
    private val restTemplate: RestTemplate
) : TelegramCoreAbstract(restTemplate) {

    fun sendMessage(username: String, message: String): ResponseEntity<JsonNode?> {

        val request = JsonNodeFactory.instance.objectNode().apply {
            put("chat_id", "@$username")
            put("text", message)
        }

        val logToken: String = LogUtils.logHttpRequest(this.javaClass, "sendMessage", request)
        val responseEntity = super.post("sendMessage", null, request)
        responseEntity.body?.also {
            LogUtils.logHttpResponse(logToken, this.javaClass, it)
        }

        return responseEntity
    }

}