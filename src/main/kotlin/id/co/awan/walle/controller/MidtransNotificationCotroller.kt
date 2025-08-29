package id.co.awan.walle.controller

import com.fasterxml.jackson.databind.JsonNode
import id.co.awan.walle.business.MidtransNotificationBusiness
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/midtrans/notification")
class MidtransNotificationCotroller(
    private val business: MidtransNotificationBusiness
) {

    @PostMapping(
        path = ["/payment"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun paymentNotification(@RequestBody request: JsonNode): ResponseEntity<Unit> =
        ResponseEntity.ok(business.paymentNotification(request))
}