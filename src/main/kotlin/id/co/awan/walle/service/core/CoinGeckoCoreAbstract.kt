package id.co.awan.walle.service.core

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.util.UriComponentsBuilder

abstract class CoinGeckoCoreAbstract(
    private val restTemplate: RestTemplate
) {

    @Value("\${coingecko.host}")
    private lateinit var coinGeckoHost: String

    @Value("\${coingecko.api-key}")
    private lateinit var apiKey: String

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
        coinGeckoPath: String,
        queryParams: LinkedMultiValueMap<String, String>? = null,
        body: JsonNode? = null,
    ): ResponseEntity<JsonNode?> {

        val url = UriComponentsBuilder.fromUriString(coinGeckoHost + coinGeckoPath)
            .queryParams(queryParams)
            .build()
            .toUri()

        val headers = HttpHeaders()
        headers.accept = listOf(MediaType.APPLICATION_JSON)
        headers["x-cg-demo-api-key"] = apiKey

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

            val errorMessage1 = responseJson.at("/error").asText("General Error")
            if (errorMessage1 != null) {
                throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "01|$errorMessage1")
            }

            val errorCode = responseJson.at("/status/error_code").asInt()
            val errorMessage2 = responseJson.at("/status/error_message").asText("General Error")
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "$errorCode|$errorMessage2")
        }

        return responseJson
    }


}