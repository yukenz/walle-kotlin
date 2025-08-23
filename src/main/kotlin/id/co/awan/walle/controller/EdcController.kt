package id.co.awan.walle.controller

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import id.co.awan.walle.service.EthMiddlewareService
import id.co.awan.walle.service.HSMService
import id.co.awan.walle.service.Tap2PayService
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.lang.String
import java.security.SignatureException
import kotlin.Throws
import kotlin.arrayOf

@RestController
@RequestMapping("/api/v1/edc")
class EdcController(
    val tap2PayService: Tap2PayService,
    val hsmService: HSMService,
    val ethMiddlewareService: EthMiddlewareService
) {

    @Operation(summary = "Do Payment Request")
    @PostMapping(
        path = ["/payment-request"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun paymentRequest(
        @RequestBody request: JsonNode
    ): ResponseEntity<JsonNode?> {

        val merchantId = request.at("/merchantId").asText(null)
        val merchantKey = request.at("/merchantKey").asText(null)
        val terminalId = request.at("/terminalId").asText(null)
        val terminalKey = request.at("/terminalKey").asText(null)
        val hashCard = request.at("/hashCard").asText(null)
        val hashPin = request.at("/hashPin").asText(null)
        val paymentAmount = request.at("/paymentAmount").asText(null)

        // Validate Terminal
        val terminal = tap2PayService.validateTerminal(
            terminalId,
            terminalKey
        )

        // Validate Merchant
        val merchant = tap2PayService.validateMerchant(
            terminal,
            merchantId,
            merchantKey
        )

        // Find HSM
        val hsm = hsmService.getHsm(hashCard, hashPin)

        // Construct Json Response
        val response = JsonNodeFactory.instance.objectNode()
        response.put(
            "fromAddress", hsm.ownerAddress ?: throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, "01|hsm.ownerAddress should not be null"
            )
        )
        response.put("toAddress", merchant.address)
        response.put(
            "secretKey", hsm.secretKey ?: throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, "01|hsm.secretKey should not be null"
            )
        )

        return ResponseEntity.ok(response)
    }

    @Operation(summary = "Do Pre Payment, before TransferFrom")
    @PostMapping(
        path = ["/card-gass-recovery"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.TEXT_PLAIN_VALUE]
    )
    @Throws(
        SignatureException::class
    )
    fun cardGassRecovery(
        @RequestBody request: JsonNode
    ): ResponseEntity<String?> {

        val merchantId = request.at("/merchantId").asText(null)
        val merchantKey = request.at("/merchantKey").asText(null)
        val terminalId = request.at("/terminalId").asText(null)
        val terminalKey = request.at("/terminalKey").asText(null)
        val hashCard = request.at("/hashCard").asText(null)
        val hashPin = request.at("/hashPin").asText(null)
        val ethSignMessage = request.at("/ethSignMessage").asText(null)
        val ownerAddress = request.at("/ownerAddress").asText(null)
        val cardAddress = request.at("/cardAddress").asText(null)
        val chain = request.at("/chain").asText(null)

        // Validate Signature
        val message = String.format(
            "CARD_GASS_RECOVERY|%s|%s|%s|%s|%s|%s",
            hashCard,
            hashPin,
            merchantId,
            merchantKey,
            terminalId,
            terminalKey
        )

        ethMiddlewareService.validateEthSign(message, cardAddress, ethSignMessage)

        // Validate Terminal
        val terminal = tap2PayService.validateTerminal(
            terminalId,
            terminalKey
        )

        // Validate Merchant
        tap2PayService.validateMerchant(
            terminal,
            merchantId,
            merchantKey
        )

        // Validate Card is Valid Walle
        hsmService.getHsm(hashCard, hashPin, ownerAddress)

        // Try to recover card gas availability
        ethMiddlewareService.recoverCardGasAvailability(chain, cardAddress)
        return ResponseEntity.ok(null)
    }

}