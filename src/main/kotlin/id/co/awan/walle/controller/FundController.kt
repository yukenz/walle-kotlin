package id.co.awan.walle.controller

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import id.co.awan.walle.service.MidtransService
import id.co.awan.walle.service.RampTransactionService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.math.BigInteger

@RestController
@RequestMapping("/api/fund")
class FundController(
    private val rampTransactionService: RampTransactionService,
    private val midtransService: MidtransService,
    private val objectMapper: ObjectMapper
) {

    @GetMapping(
        path = ["/inquiry-on-ramp"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun inquiyOnRamp(
        @RequestParam address: String
    ): ResponseEntity<JsonNode?> {
        // TODO: IMPLEMENT LOGIC
        // Cek Pending ORRDER-ID based on address
        return ResponseEntity.ok<JsonNode?>(null)
    }

    @PostMapping(
        path = ["/create-on-ramp"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun createOnRamp(
        @RequestBody request: JsonNode
    ): ResponseEntity<JsonNode> {

        // TODO : Validate this field
        val firstName = request.at("/first_name").asText(null)
        val lastName = request.at("/last_name").asText(null)
        val email = request.at("/email").asText(null)
        val phone = request.at("/phone").asText(null)
        val walletAddress = request.at("/wallet_address").asText(null)
        val chain = request.at("/chain").asText(null)
        val erc20Address = request.at("/erc20_address").asText(null)
        val amount = request.at("/amount").asInt()

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
                return ResponseEntity.ok(vaDetail)
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