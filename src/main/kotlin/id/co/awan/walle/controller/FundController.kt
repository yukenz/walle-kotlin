package id.co.awan.walle.controller

import com.fasterxml.jackson.databind.JsonNode
import id.co.awan.walle.business.FundBusiness
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/fund")
class FundController(
    private val business: FundBusiness
) {

    @GetMapping(
        path = ["/inquiry-on-ramp"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun inquiyOnRamp(@RequestParam address: String): ResponseEntity<JsonNode?> =
    // TODO: IMPLEMENT LOGIC
        // Cek Pending ORRDER-ID based on address
        ResponseEntity.ok(business.inquiyOnRamp(address))


    @PostMapping(
        path = ["/create-on-ramp"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun createOnRamp(@RequestBody request: JsonNode): ResponseEntity<JsonNode> =
        ResponseEntity.ok(business.createOnRamp(request))

}