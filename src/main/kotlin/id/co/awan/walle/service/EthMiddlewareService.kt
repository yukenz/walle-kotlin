package id.co.awan.walle.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import id.co.awan.walle.service.core.Web3MiddlewareCoreAbstract
import id.co.awan.walle.utils.LogUtils
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.server.ResponseStatusException

@Service
class EthMiddlewareService(restTemplate: RestTemplate) : Web3MiddlewareCoreAbstract(restTemplate) {

    @Throws(Exception::class)
    private fun validateEthSign(
        message: String,
        address: String,
        signature: String
    ): Unit {

        val request = JsonNodeFactory.instance.objectNode()
        request.put("message", message)
        request.put("signature", signature)

        val reqToken = LogUtils.logHttpRequest(this.javaClass, "validateEthSign", request)
        val responseEntity: ResponseEntity<JsonNode?> =
            super.post("/api/web3/validateEthSign", null, request)
        val responseJson = super.parseResponseJsonNode(responseEntity)
        LogUtils.logHttpResponse(reqToken, this.javaClass, responseJson)

        val data = responseJson.at("/data").asText(null)
            ?: throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, "01|data should not be null"
            )

        if (data == address) {
            throw ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "Signature ECRecover is not valid"
            )
        }

    }

}