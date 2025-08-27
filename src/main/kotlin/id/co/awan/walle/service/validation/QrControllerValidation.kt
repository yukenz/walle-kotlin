package id.co.awan.walle.service.validation

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import id.co.awan.walle.service.core.ValidationCoreAbstract
import jakarta.validation.Validator
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.hibernate.validator.constraints.Length
import org.springframework.stereotype.Service
import java.math.BigInteger

@Service
class QrControllerValidation(
    validator: Validator,
    private val objectMapper: ObjectMapper
) : ValidationCoreAbstract(validator) {

    data class QrPaymentRequestPayload(

        @field:NotNull
        @field:NotBlank
        val merchantId: String,

        @field:NotNull
        @field:NotBlank
        @field:Length(min = 42, max = 42)
        val cardAddress: String,

        @field:NotNull
        @field:NotBlank
        val amount: BigInteger,

        @field:NotNull
        @field:NotBlank
        val chain: String,

        @field:NotNull
        @field:NotBlank
        @field:Length(min = 42, max = 42)
        val erc20Address: String,

        @field:NotNull
        @field:NotBlank
        val hashCard: String,

        @field:NotNull
        @field:NotBlank
        val hashPin: String,

        @field:NotNull
        @field:NotBlank
        val ethSignMessage: String,
    )

    fun validateQrPayment(request: JsonNode): QrPaymentRequestPayload {
        val requestPojo = objectMapper.treeToValue(request, QrPaymentRequestPayload::class.java)
        super.validate(requestPojo)
        return requestPojo
    }


}