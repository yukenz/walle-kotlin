package id.co.awan.walle.controller

import com.fasterxml.jackson.databind.JsonNode
import id.co.awan.walle.constant.CardSelfServiceOperation
import id.co.awan.walle.service.web3middleware.Eip712MiddlewareService
import id.co.awan.walle.service.web3middleware.EthMiddlewareService
import id.co.awan.walle.service.dao.HSMService
import id.co.awan.walle.service.dao.MerchantService
import id.co.awan.walle.service.validation.CardControllerValidation
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/v1/card")
class CardController(
    private val merchantService: MerchantService,
    private val eip712MiddlewareService: Eip712MiddlewareService,
    private val hsmService: HSMService,
    private val ethMiddlewareService: EthMiddlewareService,
    private val validator: CardControllerValidation
) {

    @Operation(summary = "Query Cards")
    @PostMapping(
        path = ["/cards"],
        consumes = [MediaType.TEXT_PLAIN_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getCards(
        @RequestBody ethSignMessage: String
    ): ResponseEntity<MutableList<String>> {

        // Validate Signature
        val addressRecovered = ethMiddlewareService.ecRecover("CARD_QUERY", ethSignMessage)

        // Query Cards
        val cards = hsmService.getCards(addressRecovered)

        return ResponseEntity.ok(cards)
    }

    @Operation(summary = "RegisterCard")
    @PostMapping(
        path = ["/register"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.TEXT_PLAIN_VALUE]
    )
    fun registerCard(
        @RequestBody request: JsonNode
    ): ResponseEntity<Any?> {

        // Destruct Request
        val (chain, hashCard, hashPin, ethSignMessage, signerAddress) = validator.validateRegisterCard(request)

        // Validate Signature
        val recoveredAddress = eip712MiddlewareService.validateSignerCardSelfService(
            chain,
            CardSelfServiceOperation.REGISER,
            hashCard,
            hashPin,
            ethSignMessage,
            signerAddress
        )

        // Create HSM
        hsmService.createCard(
            hashCard, hashPin,
            ownerAddress = recoveredAddress
        )

        return ResponseEntity.ok(null)
    }

    @Operation(summary = "Access Card")
    @PostMapping(
        path = ["/access"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.TEXT_PLAIN_VALUE]
    )
    fun accessCard(
        @RequestBody request: JsonNode
    ): ResponseEntity<String?> {

        // Destruct Request
        val (chain, hashCard, hashPin, ethSignMessage, signerAddress) = validator.validateAccessCard(request)

        // Validate Signature
        val recoveredAddress = eip712MiddlewareService.validateSignerCardSelfService(
            chain,
            CardSelfServiceOperation.ACCESS,
            hashCard,
            hashPin,
            ethSignMessage,
            signerAddress
        )

        // Find HSM
        val hsm = hsmService.getHsm(
            hashCard,
            hashPin,
            recoveredAddress
        )
        return ResponseEntity.ok(hsm.secretKey)
    }

    @Operation(summary = "Access Card")
    @PostMapping(
        path = ["/change-pin"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.TEXT_PLAIN_VALUE]
    )
    fun changePin(
        @RequestBody request: JsonNode
    ): ResponseEntity<Any?> {

        // Destruct Request
        val (chain, hashCard, hashPin, ethSignMessage, newHashPin, signerAddress) = validator.validateChangePin(request)

        // Validate Signature
        val recoveredAddress = eip712MiddlewareService.validateSignerCardSelfService(
            chain,
            CardSelfServiceOperation.CHANGE_PIN,
            hashCard,
            hashPin,
            ethSignMessage,
            signerAddress
        )

        // Change PIN
        hsmService.changePin(
            hashCard,
            hashPin,
            newHashPin,
            recoveredAddress
        )

        return ResponseEntity.ok(null)
    }
}