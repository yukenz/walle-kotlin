package id.co.awan.walle.service

import com.fasterxml.jackson.databind.JsonNode
import id.co.awan.walle.service.core.MidtransCoreAbstract
import id.co.awan.walle.utils.LogUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class MidtransService(
    private val restTemplate: RestTemplate
) : MidtransCoreAbstract(restTemplate) {

    @Value("\${midtrans.path.transaction}")
    private lateinit var transactionPath: String

    fun createTransaction(
        orderId: String,
        grossAmount: Int,
        secure: Boolean,
        firstName: String,
        lastName: String,
        email: String,
        phone: String
    ): ResponseEntity<JsonNode?> {

        val request: JsonNode = super.generateCreateTransactionRequest(
            orderId,
            grossAmount,
            secure,
            firstName,
            lastName,
            email,
            phone
        )

        val logToken: String = LogUtils.logHttpRequest(this.javaClass, "createTransaction", request)
        val responseEntity: ResponseEntity<JsonNode?> = post(transactionPath, null, request)
        val response: JsonNode = super.parseResponseJsonNode(responseEntity)
        LogUtils.logHttpResponse(logToken, this.javaClass, response)

        /*
        {
            "token": "{{snap_token}}",
            "redirect_url": "https://app.sandbox.midtrans.com/snap/v3/redirection/{{snap_token}}"
        }
        */
        return responseEntity
    }

}