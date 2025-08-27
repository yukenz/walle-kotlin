package id.co.awan.walle.service.validation

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import id.co.awan.walle.service.core.ValidationCoreAbstract
import jakarta.validation.Validator
import org.springframework.stereotype.Service

@Service
class FundControllerValidation(
    validator: Validator,
    private val objectMapper: ObjectMapper
) : ValidationCoreAbstract(validator) {

    data class CreateOnRampRequestPayload(
        val firstName: String,
        val lastName: String,
        val email: String,
        val phone: String,
        val walletAddress: String,
        val chain: String,
        val erc20Address: String,
        val amount: Int,
    )

    fun validateCreateOnRamp(request: JsonNode): CreateOnRampRequestPayload {
        val requestPojo = objectMapper.treeToValue(request, CreateOnRampRequestPayload::class.java)
        super.validate(requestPojo)
        return requestPojo
    }

}

