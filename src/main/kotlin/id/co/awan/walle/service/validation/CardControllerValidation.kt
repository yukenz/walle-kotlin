package id.co.awan.walle.service.validation

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import id.co.awan.walle.service.core.ValidationCoreAbstract
import jakarta.validation.Validator
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.hibernate.validator.constraints.Length
import org.springframework.stereotype.Service

@Service
class CardControllerValidation(
    validator: Validator,
    private val objectMapper: ObjectMapper
) : ValidationCoreAbstract(validator) {

    data class RegisterCardRequestPayload(

        @field:NotNull
        @field:NotBlank
        val chain: String,

        @field:NotNull
        @field:NotBlank
        val hashCard: String,

        @field:NotNull
        @field:NotBlank
        val hashPin: String,

        @field:NotNull
        @field:NotBlank
        val ethSignMessage: String,

        @field:NotNull
        @field:NotBlank
        @field:Length(min = 42, max = 42)
        val signerAddress: String
    )

    data class AccessCardRequestPayload(

        @field:NotNull
        @field:NotBlank
        val chain: String,

        @field:NotNull
        @field:NotBlank
        val hashCard: String,

        @field:NotNull
        @field:NotBlank
        val hashPin: String,

        @field:NotNull
        @field:NotBlank
        val ethSignMessage: String,

        @field:NotNull
        @field:NotBlank
        @field:Length(min = 42, max = 42)
        val signerAddress: String
    )

    data class ChangePinRequestPayload(
        @field:NotNull
        @field:NotBlank
        val chain: String,

        @field:NotNull
        @field:NotBlank
        val hashCard: String,

        @field:NotNull
        @field:NotBlank
        val hashPin: String,

        @field:NotNull
        @field:NotBlank
        val ethSignMessage: String,

        @field:NotNull
        @field:NotBlank
        val newHashPin: String,

        @field:NotNull
        @field:NotBlank
        @field:Length(min = 42, max = 42)
        val signerAddress: String
    )

    fun validateRegisterCard(request: JsonNode): RegisterCardRequestPayload {
        val requestPojo = objectMapper.treeToValue(request, RegisterCardRequestPayload::class.java)
        super.validate(requestPojo)
        return requestPojo
    }

    fun validateAccessCard(request: JsonNode): AccessCardRequestPayload {
        val requestPojo = objectMapper.treeToValue(request, AccessCardRequestPayload::class.java)
        super.validate(requestPojo)
        return requestPojo
    }

    fun validateChangePin(request: JsonNode): ChangePinRequestPayload {
        val requestPojo = objectMapper.treeToValue(request, ChangePinRequestPayload::class.java)
        super.validate(requestPojo)
        return requestPojo
    }

}