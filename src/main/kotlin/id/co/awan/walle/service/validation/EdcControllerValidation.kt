package id.co.awan.walle.service.validation

import com.fasterxml.jackson.databind.JsonNode
import id.co.awan.walle.utils.JsonNodeUtils
import org.springframework.stereotype.Service

@Service
class EdcControllerValidation {

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

        val merchantId = JsonNodeUtils.validateField(request, "/merchantId", String::class)!!
        val merchantKey = JsonNodeUtils.validateField(request, "/merchantKey", String::class)!!
        val terminalId = JsonNodeUtils.validateField(request, "/terminalId", String::class)!!
        val terminalKey = JsonNodeUtils.validateField(request, "/terminalKey", String::class)!!

        return MerchantInquiryRequestPayload(
            merchantId = merchantId,
            merchantKey = merchantKey,
            terminalId = terminalId,
            terminalKey = terminalKey
        )
    }

    fun validatePaymentRequest(request: JsonNode): PaymentRequestRequestPayload {

        val merchantId = JsonNodeUtils.validateField(request, "/merchantId", String::class)!!
        val merchantKey = JsonNodeUtils.validateField(request, "/merchantKey", String::class)!!
        val terminalId = JsonNodeUtils.validateField(request, "/terminalId", String::class)!!
        val terminalKey = JsonNodeUtils.validateField(request, "/terminalKey", String::class)!!
        val hashCard = JsonNodeUtils.validateField(request, "/hashCard", String::class)!!
        val hashPin = JsonNodeUtils.validateField(request, "/hashPin", String::class)!!
        val paymentAmount = JsonNodeUtils.validateField(request, "/paymentAmount", String::class)!!

        return PaymentRequestRequestPayload(
            merchantId = merchantId,
            merchantKey = merchantKey,
            terminalId = terminalId,
            terminalKey = terminalKey,
            hashCard = hashCard,
            hashPin = hashPin,
            paymentAmount = paymentAmount
        )
    }

    fun validateCardGassRecovery(request: JsonNode): CardGassRecoveryRequestPayload {

        val terminalId = JsonNodeUtils.validateField(request, "/terminalId", String::class)!!
        val terminalKey = JsonNodeUtils.validateField(request, "/terminalKey", String::class)!!
        val merchantId = JsonNodeUtils.validateField(request, "/merchantId", String::class)!!
        val merchantKey = JsonNodeUtils.validateField(request, "/merchantKey", String::class)!!
        val hashCard = JsonNodeUtils.validateField(request, "/hashCard", String::class)!!
        val hashPin = JsonNodeUtils.validateField(request, "/hashPin", String::class)!!
        val ethSignMessage = JsonNodeUtils.validateField(request, "ethSignMessage", String::class)!!
        val ownerAddress = JsonNodeUtils.validateField(request, "/ownerAddress", String::class)!!
        val cardAddress = JsonNodeUtils.validateField(request, "/cardAddress", String::class)!!
        val chain = JsonNodeUtils.validateField(request, "/chain", String::class)!!


        return CardGassRecoveryRequestPayload(
            terminalId = terminalId,
            terminalKey = terminalKey,
            merchantId = merchantId,
            merchantKey = merchantKey,
            hashCard = hashCard,
            hashPin = hashPin,
            ethSignMessage = ethSignMessage,
            ownerAddress = ownerAddress,
            cardAddress = cardAddress,
            chain = chain
        )
    }
}

