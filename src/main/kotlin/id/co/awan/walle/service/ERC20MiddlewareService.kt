package id.co.awan.walle.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import id.co.awan.walle.service.core.Web3MiddlewareCoreAbstract
import id.co.awan.walle.utils.LogUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.server.ResponseStatusException
import java.math.BigInteger

@Service
class ERC20MiddlewareService(
    private val restTemplate: RestTemplate
) : Web3MiddlewareCoreAbstract(restTemplate) {

    enum class ScOperation {
        SIMULATE,
        WRITE;
    }

    @Value("\${web3-mdw.master-key-wallet}")
    private lateinit var masterPrivateKey: String


    /*
     * =================================================================================================================
     * UTILITY
     * =================================================================================================================
     */

    fun parseAmountDecimal(
        chain: String,
        amount: BigInteger,
        erc20Address: String
    ): BigInteger {
        val decimals = this.decimals(chain, erc20Address)
        return amount.multiply(BigInteger.TEN.pow(decimals))
    }

    /*
     * =================================================================================================================
     * INQUIRY
     * =================================================================================================================
     */

    fun totalSupply(
        chain: String,
        erc20Address: String
    ): BigInteger {

        val request = JsonNodeFactory.instance.objectNode().apply {
            put("chain", chain)
            put("erc20Address", erc20Address)
        }

        val reqToken = LogUtils.logHttpRequest(this.javaClass, "totalSupply", request)
        val responseEntity: ResponseEntity<JsonNode?> = super.post("/api/web3/erc20/read/totalSupply", null, request)
        val responseJson = super.parseResponseJsonNode(responseEntity)
        LogUtils.logHttpResponse(reqToken, this.javaClass, responseJson)

        val data = responseJson.at("/data").asText(null)
            ?: throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, "01|data should not be null"
            )

        return BigInteger(data)
    }

    fun allowance(
        chain: String,
        erc20Address: String,
        sourceAddress: String,
        destinationAddress: String
    ): BigInteger {

        val request = JsonNodeFactory.instance.objectNode().apply {
            put("chain", chain)
            put("erc20Address", erc20Address)
            put("sourceAddress", sourceAddress)
            put("destinationAddress", destinationAddress)
        }

        val reqToken: String = LogUtils.logHttpRequest(this.javaClass, "allowance", request)
        val responseEntity: ResponseEntity<JsonNode?> = post("/api/web3/erc20/read/allowance", null, request)
        val responseJson = super.parseResponseJsonNode(responseEntity)
        LogUtils.logHttpResponse(reqToken, this.javaClass, responseJson)

        val data = responseJson.at("/data").asText(null)
            ?: throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, "01|data should not be null"
            )

        return BigInteger(data)
    }

    fun decimals(
        chain: String,
        erc20Address: String,
    ): Int {

        val request = JsonNodeFactory.instance.objectNode().apply {
            put("chain", chain)
            put("erc20Address", erc20Address)
        }

        val reqToken: String = LogUtils.logHttpRequest(this.javaClass, "decimals", request)
        val responseEntity: ResponseEntity<JsonNode?> = post("/api/web3/erc20/read/decimals", null, request)
        val responseJson = super.parseResponseJsonNode(responseEntity)
        LogUtils.logHttpResponse(reqToken, this.javaClass, responseJson)


        val data = responseJson.at("/data").also {
            if (it.isNull) {
                throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "01|data should not be null")
            }
        }.asInt()

        return data
    }

    /*
     * =================================================================================================================
     * TRANSACTION
     * =================================================================================================================
     */
    fun transfer(
        chain: String,
        erc20Address: String,
        destinationAddress: String,
        amount: String,
        scOperation: ScOperation
    ): String? {
        return transfer(
            chain,
            masterPrivateKey,
            erc20Address,
            destinationAddress,
            amount,
            scOperation
        )
    }

    fun transfer(
        chain: String,
        privateKey: String,
        erc20Address: String,
        destinationAddress: String,
        amount: String,
        scOperation: ScOperation
    ): String {

        val request = JsonNodeFactory.instance.objectNode().apply {
            put("chain", chain)
            put("privateKey", privateKey)
            put("erc20Address", erc20Address)
            put("destinationAddress", destinationAddress)
            put("amount", amount)
        }

        val urlPath = when (scOperation) {
            ScOperation.SIMULATE -> "/api/web3/erc20/simulate/transfer"
            ScOperation.WRITE -> "/api/web3/erc20/write/transfer"
        }

        val reqToken: String = LogUtils.logHttpRequest(this.javaClass, "transfer", request)
        val responseEntity: ResponseEntity<JsonNode?> = post(urlPath, null, request)
        val responseJson = super.parseResponseJsonNode(responseEntity)
        LogUtils.logHttpResponse(reqToken, this.javaClass, responseJson)

        return when (scOperation) {
            ScOperation.SIMULATE -> responseJson.at("/estimateWei").asText(null)
                ?: throw ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "01|estimateWei should not be null"
                )

            ScOperation.WRITE -> responseJson.at("/trxReceipt").asText(null)
                ?: throw ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "01|trxReceipt should not be null"
                )

        }
    }

    fun transferFrom(
        chain: String,
        privateKey: String,
        erc20Address: String,
        sourceAddress: String,
        destinationAddress: String,
        amount: String,
        scOperation: ScOperation
    ): String {

        val request = JsonNodeFactory.instance.objectNode().apply {
            put("chain", chain)
            put("privateKey", privateKey)
            put("erc20Address", erc20Address)
            put("sourceAddress", sourceAddress)
            put("destinationAddress", destinationAddress)
            put("amount", amount)
        }

        val urlPath = when (scOperation) {
            ScOperation.SIMULATE -> "/api/web3/erc20/simulate/transferFrom"
            ScOperation.WRITE -> "/api/web3/erc20/write/transferFrom"
        }

        val reqToken: String = LogUtils.logHttpRequest(this.javaClass, "transferFrom", request)
        val responseEntity: ResponseEntity<JsonNode?> = post(urlPath, null, request)
        val responseJson = super.parseResponseJsonNode(responseEntity)
        LogUtils.logHttpResponse(reqToken, this.javaClass, responseJson)

        val trxReceipt = responseJson.at("/trxReceipt").asText(null)
            ?: throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, "01|trxReceipt should not be null"
            )

        val estimateWei = responseJson.at("/estimateWei").asText(null)
            ?: throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, "01|estimateWei should not be null"
            )

        return when (scOperation) {
            ScOperation.SIMULATE -> estimateWei
            ScOperation.WRITE -> trxReceipt
        }
    }

    fun approve(
        chain: String,
        privateKey: String,
        erc20Address: String,
        destinationAddress: String,
        amount: String,
        scOperation: ScOperation
    ): String {

        val request = JsonNodeFactory.instance.objectNode().apply {
            put("chain", chain)
            put("privateKey", privateKey)
            put("erc20Address", erc20Address)
            put("destinationAddress", destinationAddress)
            put("amount", amount)
        }

        val urlPath = when (scOperation) {
            ScOperation.SIMULATE -> "/api/web3/erc20/simulate/approve"
            ScOperation.WRITE -> "/api/web3/erc20/write/approve"
        }

        val reqToken: String = LogUtils.logHttpRequest(this.javaClass, "approve", request)
        val responseEntity: ResponseEntity<JsonNode?> = post(urlPath, null, request)
        val responseJson = super.parseResponseJsonNode(responseEntity)
        LogUtils.logHttpResponse(reqToken, this.javaClass, responseJson)

        val trxReceipt: String = responseJson.at("/trxReceipt").asText(null)
            ?: throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, "01|trxReceipt should not be null"
            )

        val estimateWei = responseJson.at("/estimateWei").asText(null)
            ?: throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, "01|estimateWei should not be null"
            )

        return when (scOperation) {
            ScOperation.SIMULATE -> estimateWei
            ScOperation.WRITE -> trxReceipt
        }
    }

}