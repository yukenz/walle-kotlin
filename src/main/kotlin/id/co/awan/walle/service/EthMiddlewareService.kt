package id.co.awan.walle.service

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import id.co.awan.walle.service.core.Web3MiddlewareCoreAbstract
import id.co.awan.walle.utils.LogUtils
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.server.ResponseStatusException
import java.math.BigInteger

@Service
class EthMiddlewareService(
    private val restTemplate: RestTemplate
) : Web3MiddlewareCoreAbstract(restTemplate) {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }

    /* =================================================================================================================
     *  Utility
     * ============================================================================================================== */

    fun validateEthSign(
        message: String,
        address: String,
        signature: String
    ): String {

        val recoveredAddress = ecRecover(message, signature)
        if (!address.equals(recoveredAddress, ignoreCase = true)) {
            throw ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "Signature ECRecover is not valid"
            )
        }
        return recoveredAddress;
    }

    fun ecRecover(
        message: String,
        signature: String
    ): String {

        val request = JsonNodeFactory.instance.objectNode().apply {
            put("message", message)
            put("signature", signature)
        }

        val reqToken = LogUtils.logHttpRequest(this.javaClass, "ecRecover", request)
        val responseEntity = super.post("/api/web3/eth/read/ecRecover", null, request)
        val responseJson = super.parseResponseJsonNode(responseEntity)
        LogUtils.logHttpResponse(reqToken, this.javaClass, responseJson)

        val data = responseJson.at("/data").asText(null)
        data
            ?: throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, "01|data should not be null"
            )

        return data
    }

    /* =================================================================================================================
    *  Read Only
    * ============================================================================================================== */


    fun balanceOf(
        chain: String,
        address: String,
    ): BigInteger {

        val request = JsonNodeFactory.instance.objectNode().apply {
            put("chain", chain)
            put("address", address)
        }

        val reqToken = LogUtils.logHttpRequest(this.javaClass, "balanceOf", request)
        val responseEntity = super.post("/api/web3/eth/read/balanceOf", null, request)
        val responseJson = super.parseResponseJsonNode(responseEntity)
        LogUtils.logHttpResponse(reqToken, this.javaClass, responseJson)

        val data = responseJson.at("/data").asText(null)
        data
            ?: throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, "01|data should not be null"
            )

        return BigInteger(data)
    }

    fun gasPrice(
        chain: String,
    ): BigInteger {

        val request = JsonNodeFactory.instance.objectNode().apply {
            put("chain", chain)
        }

        val reqToken = LogUtils.logHttpRequest(this.javaClass, "gasPrice", request)
        val responseEntity = super.post("/api/web3/eth/read/gasPrice", null, request)
        val responseJson = super.parseResponseJsonNode(responseEntity)
        LogUtils.logHttpResponseWithoutToken("gasPrice", this.javaClass, responseJson)

        val data = responseJson.at("/data").asText(null)
        data
            ?: throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, "01|data should not be null"
            )

        return BigInteger(data)
    }

    fun gasFeeEIP1559(
        chain: String,
        toAddress: String,
        amount: BigInteger,
        unit: String
    ): String {

        val request = JsonNodeFactory.instance.objectNode().apply {
            put("chain", chain)
            put("privateKey", super.web3MasterPrivateKey)
            put("toAddress", toAddress)
            put("amount", amount.toString())
            put("unit", unit)
        }

        val reqToken = LogUtils.logHttpRequest(this.javaClass, "gasFeeEIP1559", request)
        val responseEntity = super.post("/api/web3/eth/simulate/transferEIP1559", null, request)
        val responseJson = super.parseResponseJsonNode(responseEntity)
        LogUtils.logHttpResponse(reqToken, this.javaClass, responseJson)

        val gasFee = responseJson.at("/gasFee").asText(null)

        gasFee
            ?: throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, "01|gasFee should not be null"
            )

        return gasFee
    }


    /* =================================================================================================================
    *  Transactional
    * ============================================================================================================== */

    fun recoverCardGasAvailability(
        chain: String,
        cardAddress: String
    ) {

        val cardBalance: BigInteger = balanceOf(chain, cardAddress)
        val gasFee: BigInteger = gasPrice(chain)
            .multiply(BigInteger.valueOf(70000L)) // Safe gas 70000L for transferFrom

        if (cardBalance < gasFee) {

            val amountForRecover = gasFee.subtract(cardBalance)

            // TODO: Handle this later
            gasFeeEIP1559(chain, cardAddress, amountForRecover, "WEI")
            val transactionReceipt = transferEIP1559(chain, cardAddress, amountForRecover, "WEI")

            log.info(
                "Gas fee recover from card adress {} with amount [{} wei], trx id[{}]",
                cardAddress,
                amountForRecover,
                transactionReceipt
            )

        } else {
            log.info(
                "Gas fee is safety state for card address: {} with balance {}",
                cardAddress,
                cardBalance
            )
        }
    }

    fun transferEIP1559(
        chain: String,
        toAddress: String,
        amount: BigInteger,
        unit: String
    ): String {

        val request = JsonNodeFactory.instance.objectNode().apply {
            put("chain", chain)
            put("privateKey", super.web3MasterPrivateKey)
            put("toAddress", toAddress)
            put("amount", amount.toString())
            put("unit", unit)
        }

        val reqToken = LogUtils.logHttpRequest(this.javaClass, "transferEIP1559", request)
        val responseEntity = super.post("/api/web3/eth/write/transferEIP1559", null, request)
        val responseJson = super.parseResponseJsonNode(responseEntity)
        LogUtils.logHttpResponse(reqToken, this.javaClass, responseJson)

        val trxReceipt = responseJson.at("/trxReceipt").asText(null)

        trxReceipt
            ?: throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, "01|trxReceipt should not be null"
            )

        return trxReceipt

    }

}