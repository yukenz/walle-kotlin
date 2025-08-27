package id.co.awan.walle.controller

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import id.co.awan.walle.service.web3middleware.ERC20MiddlewareService
import id.co.awan.walle.service.web3middleware.EthMiddlewareService
import id.co.awan.walle.service.dao.HSMService
import id.co.awan.walle.service.dao.MerchantService
import id.co.awan.walle.service.dao.TerminalService
import id.co.awan.walle.service.validation.EdcControllerValidation
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.security.SignatureException
import kotlin.Throws
import kotlin.apply
import kotlin.arrayOf

@RestController
@RequestMapping("/api/v1/edc")
class EdcController(
    private val merchantService: MerchantService,
    private val hsmService: HSMService,
    private val ethMiddlewareService: EthMiddlewareService,
    private val erc20MiddlewareService: ERC20MiddlewareService,
    private val edcControllerValidation: EdcControllerValidation,
    private val terminalService: TerminalService
) {

    @Operation(summary = "Inquiry Merchant")
    @PostMapping(
        path = ["/merchant-inquiry"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun merchantInquiry(
        @RequestBody request: JsonNode
    ): ResponseEntity<JsonNode?> {

        val (merchantId, merchantKey, terminalId, terminalKey) = edcControllerValidation.validateMerchantInquiry(request)

        val terminal = terminalService.validateTerminal(
            terminalId,
            terminalKey
        )

        val merchant = merchantService.validateMerchant(
            terminal,
            merchantId,
            merchantKey
        )

        val response = JsonNodeFactory.instance.objectNode().apply {
            put("merchantName", merchant.name)
            put("merchantAddress", merchant.address)
        }

        return ResponseEntity.ok(response)
    }

    @Operation(summary = "Do Payment Request")
    @PostMapping(
        path = ["/payment-request"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun paymentRequest(
        @RequestBody request: JsonNode
    ): ResponseEntity<JsonNode?> {

        val (merchantId, merchantKey, terminalId, terminalKey, hashCard, hashPin, paymentAmount)
                = edcControllerValidation.validatePaymentRequest(request)

        // Validate Terminal
        val terminal = terminalService.validateTerminal(
            terminalId,
            terminalKey
        )

        // Validate Merchant
        val merchant = merchantService.validateMerchant(
            terminal,
            merchantId,
            merchantKey
        )

        erc20MiddlewareService

        // Find HSM
        val hsm = hsmService.getHsm(hashCard, hashPin)

        // Construct Json Response
        val response = JsonNodeFactory.instance.objectNode().apply {
            put(
                "fromAddress", hsm.ownerAddress ?: throw ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "01|hsm.ownerAddress should not be null"
                )
            )
            put("toAddress", merchant.address)
            put(
                "secretKey", hsm.secretKey ?: throw ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "01|hsm.secretKey should not be null"
                )
            )
        }

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

        val (terminalId, terminalKey, merchantId, merchantKey, hashCard, hashPin, ethSignMessage, ownerAddress, cardAddress, chain)
                = edcControllerValidation.validateCardGassRecovery(request)

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
        val terminal = terminalService.validateTerminal(
            terminalId,
            terminalKey
        )

        // Validate Merchant
        merchantService.validateMerchant(
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