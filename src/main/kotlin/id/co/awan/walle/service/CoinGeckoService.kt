package id.co.awan.walle.service

import com.fasterxml.jackson.databind.JsonNode
import id.co.awan.walle.service.core.CoinGeckoCoreAbstract
import id.co.awan.walle.utils.LogUtils
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal

@Service
class CoinGeckoService(
    private val restTemplate: RestTemplate
) : CoinGeckoCoreAbstract(restTemplate) {


    fun ping(): JsonNode {
        val responseEntity: ResponseEntity<JsonNode?> = super.get("/api/v3/ping", null)
        val response: JsonNode = super.parseResponseJsonNode(responseEntity)
        LogUtils.logHttpResponseWithoutToken("ping", this.javaClass, response)
        return response
    }

    fun coinPrice(
        chainIdCoinGecko: String,
        currency: String,
        erc20Address: String,
        precision: Int
    ): BigDecimal {

        val queryParams = LinkedMultiValueMap<String, String>()
        queryParams["contract_addresses"] = erc20Address
        queryParams["vs_currencies"] = currency
        queryParams["include_market_cap"] = "false"
        queryParams["include_24hr_vol"] = "false"
        queryParams["include_24hr_change"] = "false"
        queryParams["include_last_updated_at"] = "false"
        queryParams["precision"] = precision.toString()

        val responseEntity = super.get("/api/v3/simple/token_price/$chainIdCoinGecko", queryParams)
        val response = super.parseResponseJsonNode(responseEntity)
        LogUtils.logHttpResponseWithoutToken("coinPrice", this.javaClass, response)

        val price = (response.at("/${erc20Address.lowercase()}/${currency.lowercase()}").asText(null)
            ?: throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, "01|coinPrice response should not be null"
            ))

        return BigDecimal(price)
    }

}