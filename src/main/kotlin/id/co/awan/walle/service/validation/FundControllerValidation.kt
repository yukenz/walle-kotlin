package id.co.awan.walle.service.validation

import com.fasterxml.jackson.databind.JsonNode
import id.co.awan.walle.utils.JsonNodeUtils
import org.springframework.stereotype.Service

@Service
class FundControllerValidation {

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

        val firstName = JsonNodeUtils.validateField(request, "/firstName", String::class)!!
        val lastName = JsonNodeUtils.validateField(request, "/lastName", String::class)!!
        val email = JsonNodeUtils.validateField(request, "/email", String::class)!!
        val phone = JsonNodeUtils.validateField(request, "/phone", String::class)!!
        val walletAddress = JsonNodeUtils.validateField(request, "/walletAddress", String::class)!!
        val chain = JsonNodeUtils.validateField(request, "/chain", String::class)!!
        val erc20Address = JsonNodeUtils.validateField(request, "/erc20Address", String::class)!!
        val amount = JsonNodeUtils.validateField(request, "/amount", Int::class)!!

        return CreateOnRampRequestPayload(
            firstName = firstName,
            lastName = lastName,
            email = email,
            phone = phone,
            walletAddress = walletAddress,
            chain = chain,
            erc20Address = erc20Address,
            amount = amount
        )
    }

}

