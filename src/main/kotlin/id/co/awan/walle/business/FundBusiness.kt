package id.co.awan.walle.business

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import id.co.awan.walle.service.dao.RampTransactionService
import id.co.awan.walle.service.midtrans.MidtransService
import id.co.awan.walle.service.validation.FundControllerValidation
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.math.BigInteger

@Service
class FundBusiness(
    private val rampTransactionService: RampTransactionService,
    private val midtransService: MidtransService,
    private val objectMapper: ObjectMapper,
    private val fundControllerValidation: FundControllerValidation
) {

    fun inquiyOnRamp(address: String): JsonNode? {
        return null
    }

    fun createOnRamp(request: JsonNode): JsonNode {

        val (firstName, lastName, email, phone, walletAddress, chain, erc20Address, amount)
                = fundControllerValidation.validateCreateOnRamp(request)

        // Create OnRamp Phase 1
        val orderId: String = rampTransactionService.createTransactionOnRampFirstPhase(
            walletAddress,
            chain,
            erc20Address,
            BigInteger.valueOf(amount.toLong())
        )

        try {

            // Create VA
            val transaction = midtransService.createTransaction(
                orderId,
                amount,
                true,
                firstName,
                lastName,
                email,
                phone
            )

            val vaDetail = transaction.getBody() ?: throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, "vaDetail shouldn't be null"
            )

            if (transaction.statusCode == HttpStatus.CREATED) {

                // Create OnRamp Phase 2 Success
                val redirectUrl = vaDetail.at("/redirect_url").asText(null)
                val token = vaDetail.at("/token").asText(null)

                rampTransactionService.createTransactionOnRampSecondPhase(orderId, redirectUrl, token!!)
                return vaDetail
            } else {

                // Create OnRamp Phase 2 Failed
                val errorMessages = objectMapper.convertValue(
                    vaDetail.at("/error_messages"),
                    object : TypeReference<MutableList<String>>() {
                    }).joinToString(",")

                throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, errorMessages)
            }
        } catch (ex: Exception) {
            // Create OnRamp Phase 2 Failed
            rampTransactionService.errorTransactionOnRampSecondPhase(orderId, ex.message ?: "General Error")
            throw ex
        }
    }
}