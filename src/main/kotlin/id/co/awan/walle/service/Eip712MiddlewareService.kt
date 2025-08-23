package id.co.awan.walle.service

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import id.co.awan.walle.constant.CardSelfServiceOperation
import id.co.awan.walle.service.core.Web3MiddlewareCoreAbstract
import id.co.awan.walle.utils.LogUtils
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.server.ResponseStatusException

@Service
class Eip712MiddlewareService(restTemplate: RestTemplate) : Web3MiddlewareCoreAbstract(restTemplate) {

    fun getSignerCardSelfService(
        chain: String,
        operation: CardSelfServiceOperation,
        hashCard: String,
        hashPin: String,
        signature: String
    ): String {

        val request = JsonNodeFactory.instance.objectNode()
        request.put("chain", chain)
        request.put("operation", operation.ordinal)
        request.put("hashCard", hashCard)
        request.put("hashPin", hashPin)
        request.put("signature", signature)

        val reqToken = LogUtils.logHttpRequest(this.javaClass, "getSignerCardSelfService", request)
        val responseEntity = super.post("/api/web3/eip712/walle/signerCardSelfService", null, request)
        val responseJson = super.parseResponseJsonNode(responseEntity)
        LogUtils.logHttpResponse(reqToken, this.javaClass, responseJson)

        val data = responseJson.at("/data").asText(null)
            ?: throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, "01|data should not be null"
            )

        return data
    }


    fun validateSignerCardSelfService(
        chain: String,
        operation: CardSelfServiceOperation,
        hashCard: String,
        hashPin: String,
        signature: String,
        signerAddress: String
    ): String {

        val recoveredAddress = getSignerCardSelfService(chain, operation, hashCard, hashPin, signature)

        if (!recoveredAddress.equals(signerAddress, ignoreCase = true)) {
            throw ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "Signer Validation Failed"
            )
        }

        return recoveredAddress
    }


}