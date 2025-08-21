package id.co.awan.walle.service

import com.fasterxml.jackson.databind.JsonNode
import id.co.awan.walle.service.core.CoinGeckoCoreAbstract
import id.co.awan.walle.utils.LogUtils
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate

@Service
class CoinGeckoService(restTemplate: RestTemplate) : CoinGeckoCoreAbstract(restTemplate) {


    fun ping(): JsonNode {

        val responseEntity: ResponseEntity<JsonNode?> = super.get("/api/v3/ping", null)
        val response: JsonNode = super.parseResponseJsonNode(responseEntity)
        LogUtils.logHttpResponseWithoutToken("ping", this.javaClass, response)

        return response
    }

    fun coinPrice(
        currency: String,
        erc20Address: String,
        precision: Int
    ): JsonNode {

        val queryParams = LinkedMultiValueMap<String, String>()
        queryParams["contract_addresses"] = erc20Address
        queryParams["vs_currencies"] = currency
        queryParams["include_market_cap"] = "false"
        queryParams["include_24hr_vol"] = "false"
        queryParams["include_24hr_change"] = "false"
        queryParams["include_last_updated_at"] = "false"
        queryParams["precision"] = precision.toString()

        val responseEntity = super.get("/api/v3/simple/token_price/id", queryParams)
        val response: JsonNode = super.parseResponseJsonNode(responseEntity)
        LogUtils.logHttpResponseWithoutToken("coinPrice", this.javaClass, response)

        return response
    }

}