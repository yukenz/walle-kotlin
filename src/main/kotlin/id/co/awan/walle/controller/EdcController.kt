package id.co.awan.walle.controller

import com.fasterxml.jackson.databind.JsonNode
import id.co.awan.walle.business.EdcBusiness
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/edc")
class EdcController(
    private val business: EdcBusiness
) {

    @Operation(summary = "Inquiry Merchant")
    @PostMapping(
        path = ["/merchant-inquiry"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun merchantInquiry(@RequestBody request: JsonNode): ResponseEntity<JsonNode?> =
        ResponseEntity.ok(business.merchantInquiry(request))

    @Operation(summary = "Do Payment Request")
    @PostMapping(
        path = ["/payment-request"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun paymentRequest(@RequestBody request: JsonNode): ResponseEntity<JsonNode?> =
        ResponseEntity.ok(business.paymentRequest(request))


    @Operation(summary = "Do Pre Payment, before TransferFrom")
    @PostMapping(
        path = ["/card-gass-recovery"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.TEXT_PLAIN_VALUE]
    )
    fun cardGassRecovery(@RequestBody request: JsonNode): ResponseEntity<Unit> =
        ResponseEntity.ok(business.cardGassRecovery(request))

}