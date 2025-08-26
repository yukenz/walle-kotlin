package id.co.awan.walle.service.validation

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import id.co.awan.walle.service.core.ValidationCoreAbstract
import jakarta.validation.Validator
import org.springframework.stereotype.Service

@Service
class EdcControllerValidation(
    validator: Validator,
    private val objectMapper: ObjectMapper
) : ValidationCoreAbstract(validator) {

    data class MerchantInquiryRequestPayload(
        val merchantId: String,
        val merchantKey: String,
        val terminalId: String,
        val terminalKey: String,
    )

    data class PaymentRequestRequestPayload(
        val merchantId: String,
        val merchantKey: String,
        val terminalId: String,
        val terminalKey: String,
        val hashCard: String,
        val hashPin: String,
        val paymentAmount: String
    )

    data class CardGassRecoveryRequestPayload(
        val terminalId: String,
        val terminalKey: String,
        val merchantId: String,
        val merchantKey: String,
        val hashCard: String,
        val hashPin: String,
        val ethSignMessage: String,
        val ownerAddress: String,
        val cardAddress: String,
        val chain: String
    )

    fun validateMerchantInquiry(request: JsonNode): MerchantInquiryRequestPayload {
        val requestPojo = objectMapper.treeToValue(request, MerchantInquiryRequestPayload::class.java)
        super.validate(requestPojo)
        return requestPojo
    }

    fun validatePaymentRequest(request: JsonNode): PaymentRequestRequestPayload {
        val requestPojo = objectMapper.treeToValue(request, PaymentRequestRequestPayload::class.java)
        super.validate(requestPojo)
        return requestPojo
    }

    fun validateCardGassRecovery(request: JsonNode): CardGassRecoveryRequestPayload {
        val requestPojo = objectMapper.treeToValue(request, CardGassRecoveryRequestPayload::class.java)
        super.validate(requestPojo)
        return requestPojo
    }
}

