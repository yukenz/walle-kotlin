package id.co.awan.walle.service.core

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.util.UriComponentsBuilder
import java.nio.charset.StandardCharsets

abstract class MidtransCoreAbstract(
    private val restTemplate: RestTemplate
) {

    @Value("\${midtrans.host}")
    private lateinit var midtransHost: String

    @Value("\${midtrans.server-key}")
    private lateinit var serverKey: String

    fun generateCreateTransactionRequest(
        orderId: String,
        grossAmount: Int,
        secure: Boolean,
        firstName: String,
        lastName: String,
        email: String,
        phone: String
    ): JsonNode {

        /*
                  {
                    "transaction_details": {
                      "order_id": "YOUR-ORDERID-123456",
                      "gross_amount": 10000
                    },
                    "credit_card": {
                      "secure": true
                    },
                    "customer_details": {
                      "first_name": "budi",
                      "last_name": "pratama",
                      "email": "budi.pra@example.com",
                      "phone": "08111222333"
                    }
                  }
                  */

        val transactionDetails = JsonNodeFactory.instance.objectNode()
        transactionDetails.put("order_id", orderId)
        transactionDetails.put("gross_amount", grossAmount)

        val creditCard = JsonNodeFactory.instance.objectNode()
        creditCard.put("secure", secure)

        val customerDetails = JsonNodeFactory.instance.objectNode()
        customerDetails.put("first_name", firstName)
        customerDetails.put("last_name", lastName)
        customerDetails.put("email", email)
        customerDetails.put("phone", phone)

        val requestObject = JsonNodeFactory.instance.objectNode()
        requestObject.set<JsonNode>("transaction_details", transactionDetails)
        requestObject.set<JsonNode>("credit_card", creditCard)
        requestObject.set<JsonNode>("customer_details", customerDetails)

        return requestObject
    }

    protected fun get(
        coinGeckoPath: String,
        queryParams: LinkedMultiValueMap<String, String>? = null
    ): ResponseEntity<JsonNode?> {
        return executeRest(
            HttpMethod.GET,
            coinGeckoPath,
            queryParams
        )
    }

    protected fun post(
        coinGeckoPath: String,
        queryParams: LinkedMultiValueMap<String, String>? = null,
        body: JsonNode? = null,
    ): ResponseEntity<JsonNode?> {
        return executeRest(
            HttpMethod.POST,
            coinGeckoPath,
            queryParams,
            body
        )
    }

    protected fun executeRest(
        method: HttpMethod,
        midtransPath: String,
        queryParams: LinkedMultiValueMap<String, String>? = null,
        body: JsonNode? = null,
    ): ResponseEntity<JsonNode?> {

        val url = UriComponentsBuilder.fromUriString(midtransHost + midtransPath)
            .queryParams(queryParams)
            .build()
            .toUri()

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.accept = mutableListOf(MediaType.APPLICATION_JSON)
        headers.setBasicAuth(serverKey, "", StandardCharsets.UTF_8)

        return restTemplate.exchange(
            url,
            method,
            HttpEntity<JsonNode?>(body, headers),
            JsonNode::class.java
        )
    }

    protected fun parseResponseJsonNode(responseEntity: ResponseEntity<JsonNode?>): JsonNode {

        val responseJson: JsonNode = responseEntity.getBody()
            ?: throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, "01|ResponseJson should not be null"
            )

        if (responseEntity.statusCode != HttpStatus.OK) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "01" + "|" + responseEntity.statusCode)
        }

        return responseJson
    }


}