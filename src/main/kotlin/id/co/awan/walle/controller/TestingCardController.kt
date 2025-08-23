package id.co.awan.walle.controller

import id.co.awan.walle.service.HSMService
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/testing/card")
class TestingCardController(
    val hsmService: HSMService,
) {

    @Operation(summary = "Access Card")
    @PostMapping(
        path = ["/reset"],
        consumes = [MediaType.TEXT_PLAIN_VALUE],
        produces = [MediaType.TEXT_PLAIN_VALUE]
    )
    fun resetRegisteredCardForTest(
        @RequestBody hashCardUUID: String
    ): ResponseEntity<Any?> {
        hsmService.resetHsm(hashCardUUID)
        return ResponseEntity.ok(hashCardUUID)
    }

}