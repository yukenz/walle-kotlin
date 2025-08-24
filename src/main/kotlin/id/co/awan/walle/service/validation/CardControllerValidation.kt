package id.co.awan.walle.service.validation

import com.fasterxml.jackson.databind.JsonNode
import id.co.awan.walle.utils.JsonNodeUtils
import org.springframework.stereotype.Service

@Service
class CardControllerValidation {

    data class RegisterCardRequestPayload(
        val chain: String,
        val hashCard: String,
        val hashPin: String,
        val ethSignMessage: String,
        val signerAddress: String
    )

    fun validateRegisterCard(request: JsonNode): RegisterCardRequestPayload {

        val chain = JsonNodeUtils.validateField(request, "/chain", String::class)!!
        val hashCard = JsonNodeUtils.validateField(request, "/hashCard", String::class)!!
        val hashPin = JsonNodeUtils.validateField(request, "/hashPin", String::class)!!
        val ethSignMessage = JsonNodeUtils.validateField(request, "/ethSignMessage", String::class)!!
        val signerAddress = JsonNodeUtils.validateField(request, "/signerAddress", String::class)!!

        return RegisterCardRequestPayload(
            chain = chain,
            hashCard = hashCard,
            hashPin = hashPin,
            ethSignMessage = ethSignMessage,
            signerAddress = signerAddress
        )
    }


    data class AccessCardRequestPayload(
        val chain: String,
        val hashCard: String,
        val hashPin: String,
        val ethSignMessage: String,
        val signerAddress: String
    )

    fun validateAccessCard(request: JsonNode): AccessCardRequestPayload {

        val chain = JsonNodeUtils.validateField(request, "/chain", String::class)!!
        val hashCard = JsonNodeUtils.validateField(request, "/hashCard", String::class)!!
        val hashPin = JsonNodeUtils.validateField(request, "/hashPin", String::class)!!
        val ethSignMessage = JsonNodeUtils.validateField(request, "/ethSignMessage", String::class)!!
        val signerAddress = JsonNodeUtils.validateField(request, "/signerAddress", String::class)!!

        return AccessCardRequestPayload(
            chain = chain,
            hashCard = hashCard,
            hashPin = hashPin,
            ethSignMessage = ethSignMessage,
            signerAddress = signerAddress
        )
    }

    data class ChangePinRequestPayload(
        val chain: String,
        val hashCard: String,
        val hashPin: String,
        val ethSignMessage: String,
        val newHashPin: String,
        val signerAddress: String
    )

    fun validateChangePin(request: JsonNode): ChangePinRequestPayload {

        val chain = JsonNodeUtils.validateField(request, "/chain", String::class)!!
        val hashCard = JsonNodeUtils.validateField(request, "/hashCard", String::class)!!
        val hashPin = JsonNodeUtils.validateField(request, "/hashPin", String::class)!!
        val ethSignMessage = JsonNodeUtils.validateField(request, "/ethSignMessage", String::class)!!
        val newHashPin = JsonNodeUtils.validateField(request, "/newHashPin", String::class)!!
        val signerAddress = JsonNodeUtils.validateField(request, "/signerAddress", String::class)!!

        return ChangePinRequestPayload(
            chain = chain,
            hashCard = hashCard,
            hashPin = hashPin,
            ethSignMessage = ethSignMessage,
            newHashPin = newHashPin,
            signerAddress = signerAddress
        )
    }

}