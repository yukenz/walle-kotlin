package id.co.awan.walle.service.core

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.util.UriComponentsBuilder
import java.nio.charset.StandardCharsets

abstract class Web3MiddlewareCoreAbstract(
    private val restTemplate: RestTemplate,
) {

    @Value("\${web3-mdw.host}")
    private lateinit var web3MiddlewareHost: String

    @Value("\${web3-mdw.username}")
    private lateinit var web3MiddlewareUsername: String

    @Value("\${web3-mdw.password}")
    private lateinit var web3MiddlewarePassword: String

    @Value("\${web3-mdw.master-key-wallet}")
    protected lateinit var web3MasterPrivateKey: String

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
        erc20MiddlewarePath: String,
        queryParams: LinkedMultiValueMap<String, String>? = null,
        body: JsonNode? = null,
    ): ResponseEntity<JsonNode?> {

        val url = UriComponentsBuilder.fromUriString(web3MiddlewareHost + erc20MiddlewarePath)
            .queryParams(queryParams)
            .build()
            .toUri()

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.accept = mutableListOf(MediaType.APPLICATION_JSON)
        headers.setBasicAuth(web3MiddlewareUsername, web3MiddlewarePassword, StandardCharsets.UTF_8)

        return restTemplate.exchange(
            url,
            method,
            HttpEntity<JsonNode?>(body, headers),
            JsonNode::class.java
        )
    }

    protected fun parseResponseJsonNode(
        responseEntity: ResponseEntity<JsonNode?>
    ): JsonNode {

        val responseJson: JsonNode = responseEntity.getBody()
            ?: throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, "01|ResponseJson should not be null"
            )

        if (responseEntity.statusCode != HttpStatus.OK) {
            val error = responseJson.at("/errorCode").asText("01")
            val errorDetail = responseJson.at("/errorDetail").toPrettyString()
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "$error|$errorDetail")
        }

        return responseJson
    }


}