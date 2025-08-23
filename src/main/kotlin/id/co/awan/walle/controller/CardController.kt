package id.co.awan.walle.controller

import com.fasterxml.jackson.databind.JsonNode
import id.co.awan.walle.constant.CardSelfServiceOperation
import id.co.awan.walle.service.Eip712MiddlewareService
import id.co.awan.walle.service.EthMiddlewareService
import id.co.awan.walle.service.HSMService
import id.co.awan.walle.service.Tap2PayService
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
    val tap2PayService: Tap2PayService,
    val eip712MiddlewareService: Eip712MiddlewareService,
    val hsmService: HSMService,
    val ethMiddlewareService: EthMiddlewareService
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
        val cards = tap2PayService.getCards(addressRecovered)

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

        val hashCard = request.at("/hashCard").asText(null)
        val hashPin = request.at("/hashPin").asText(null)
        val ethSignMessage = request.at("/ethSignMessage").asText(null)
        val signerAddress = request.at("/signerAddress").asText(null)

        // Validate Signature
        val recoveredAddress = eip712MiddlewareService.validateSignerCardSelfService(
            CardSelfServiceOperation.REGISER,
            hashCard,
            hashPin,
            ethSignMessage,
            signerAddress
        )

        // Create HSM
        tap2PayService.createCard(
            hashCard, hashPin,
            ownerAddress = recoveredAddress
        )

        return ResponseEntity.ok(null)
    }

    @Operation(summary = "Access Card")
    @PostMapping(
        path = ["/card-access"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.TEXT_PLAIN_VALUE]
    )
    fun accessCard(
        @RequestBody request: JsonNode
    ): ResponseEntity<String?> {

        val hashCard = request.at("/hashCard").asText(null)
        val hashPin = request.at("/hashPin").asText(null)
        val ethSignMessage = request.at("/ethSignMessage").asText(null)
        val signerAddress = request.at("/signerAddress").asText(null)

        // Validate Signature
        val recoveredAddress = eip712MiddlewareService.validateSignerCardSelfService(
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

        val hashCard = request.at("/hashCard").asText(null)
        val hashPin = request.at("/hashPin").asText(null)
        val ethSignMessage = request.at("/ethSignMessage").asText(null)
        val newHashPin = request.at("/newHashPin").asText(null)
        val signerAddress = request.at("/signerAddress").asText(null)

        // Validate Signature
        val recoveredAddress = eip712MiddlewareService.validateSignerCardSelfService(
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