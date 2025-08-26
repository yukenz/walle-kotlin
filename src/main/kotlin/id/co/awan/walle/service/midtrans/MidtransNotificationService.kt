package id.co.awan.walle.service.midtrans

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.validation.Validator
import org.apache.hc.client5.http.utils.Hex
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

@Service
class MidtransNotificationService(
    private val objectMapper: ObjectMapper,
    private val validator: Validator,
) {

    @Value("\${midtrans.server-key}")
    lateinit var serverKey: String

    @Value("\${midtrans.client-key}")
    lateinit var clientKey: String

    @Value("\${midtrans.merchant-id}")
    lateinit var merchantId: String

//    val signatureValidation = object : Validator {
//        override fun supports(clazz: Class<*>): Boolean {
//            return JsonNode::class.java.isAssignableFrom(clazz)
//        }
//
//        override fun validate(target: Any, errors: Errors) {
//
//            val request = target as JsonNode
//
//            request.at("/order_id").asText(null)
//                ?: throw ResponseStatusException(
//                    HttpStatus.INTERNAL_SERVER_ERROR, "01|order_id should not be null"
//                )
//
//            request.at("/status_code").asText(null)
//                ?: throw ResponseStatusException(
//                    HttpStatus.INTERNAL_SERVER_ERROR, "01|status_code should not be null"
//                )
//
//            request.at("/gross_amount").asText(null)
//                ?: throw ResponseStatusException(
//                    HttpStatus.INTERNAL_SERVER_ERROR, "01|gross_amount should not be null"
//                )
//
//        }
//    }

    fun validateSignature(request: JsonNode) {


        val orderId = request.at("/order_id").asText()
        val statusCode = request.at("/status_code").asText()
        val grossAmount = request.at("/gross_amount").asText()

        val digest = MessageDigest.getInstance("SHA-512")
        val hashBytes = digest.digest(
            (orderId + statusCode + grossAmount + serverKey)
                .toByteArray(StandardCharsets.UTF_8)
        )

        val signatureKey = request.at("/signature_key").asText()
        val signature = Hex.encodeHexString(hashBytes)

        if (!signatureKey.equals(signature, ignoreCase = true)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Signature Key isn't valid")
        }
    }

}