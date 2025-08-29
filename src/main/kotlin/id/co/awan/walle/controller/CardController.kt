package id.co.awan.walle.controller

import com.fasterxml.jackson.databind.JsonNode
import id.co.awan.walle.business.CardBusiness
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
    private val business: CardBusiness,
) {

    @Operation(summary = "Query Cards")
    @PostMapping(
        path = ["/cards"],
        consumes = [MediaType.TEXT_PLAIN_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getCards(@RequestBody ethSignMessage: String): ResponseEntity<MutableList<String>> =
        ResponseEntity.ok(business.registerCard(ethSignMessage))

    @Operation(summary = "RegisterCard")
    @PostMapping(
        path = ["/register"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.TEXT_PLAIN_VALUE]
    )
    fun registerCard(@RequestBody request: JsonNode): ResponseEntity<Unit> =
        ResponseEntity.ok(business.registerCard(request))

    @Operation(summary = "Access Card")
    @PostMapping(
        path = ["/access"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.TEXT_PLAIN_VALUE]
    )
    fun accessCard(@RequestBody request: JsonNode): ResponseEntity<String?> =
        ResponseEntity.ok(business.accessCard(request))

    @Operation(summary = "Access Card")
    @PostMapping(
        path = ["/change-pin"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.TEXT_PLAIN_VALUE]
    )
    fun changePin(@RequestBody request: JsonNode): ResponseEntity<Unit> =
        ResponseEntity.ok(business.changePin(request))

}