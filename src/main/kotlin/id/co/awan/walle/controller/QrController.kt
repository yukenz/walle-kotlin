package id.co.awan.walle.controller

import com.fasterxml.jackson.databind.JsonNode
import id.co.awan.walle.business.QrBusiness
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/v1/qr")
class QrController(
    private val business: QrBusiness
) {

    @Operation(summary = "Query QR Merchant")
    @GetMapping(
        path = ["/inquiry"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun qrInquiry(
        @RequestParam(name = "merchantId") merchantId: String,
    ): ResponseEntity<JsonNode> =
        ResponseEntity.ok(business.qrInquiry(merchantId))

    @Operation(summary = "Query QR Merchant")
    @GetMapping(
        path = ["/metadata-inquiry"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun qrMetadataInquiry(
        @RequestParam(name = "cardAddress") cardAddress: String,
        @RequestParam(name = "hashCard") hashCard: String,
    ): ResponseEntity<*> =
        ResponseEntity.ok(business.qrMetadataInquiry(cardAddress, hashCard))


    @Operation(summary = "Payment QR Merchant")
    @PostMapping(
        path = ["/payment"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
     fun qrPayment(
        @RequestBody request: JsonNode
    ): ResponseEntity<List<String>> {
        return ResponseEntity.ok(business.qrPayment(request))
    }


}