package id.co.awan.walle.controller

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import id.co.awan.walle.service.dao.MerchantService
import id.co.awan.walle.service.validation.CardControllerValidation
import id.co.awan.walle.service.validation.QrControllerValidation
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/v1/qr")
class QrController(
    private val merchantService: MerchantService,
    private val validator: CardControllerValidation,
    private val qrControllerValidation: QrControllerValidation
) {

    @Operation(summary = "Query QR Merchant")
    @GetMapping(
        path = ["/query"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun qrInquiry(
        @RequestParam(name = "merchantId") merchantId: String,
    ): ResponseEntity<Any?> {

        val merchant = merchantService.validateMerchant(merchantId)

        val response = JsonNodeFactory.instance.objectNode().apply {
            put("id", merchant.id)
            put("name", merchant.name)
            put("address", merchant.address)
        }

        return ResponseEntity.ok(response)
    }

    @Operation(summary = "Payment QR Merchant")
    @PostMapping(
        path = ["/payment"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.TEXT_PLAIN_VALUE]
    )
    fun qrPayment(
        @RequestBody request: JsonNode
    ): ResponseEntity<Any?> {

        val (merchantId, cardAddress, amount, chain, erc20Address, hashCard, hashPin, ethSignMessage)
                = qrControllerValidation.validateQrPayment(request)

        val merchant = merchantService.validateMerchant(merchantId)


        // TODO: Implement Payment QR
        // 1. Checking allowance
        // 2. Sponsor / Recovery Card Gass
        // 3. TransferFrom

        return ResponseEntity.ok().body(null);

    }


}