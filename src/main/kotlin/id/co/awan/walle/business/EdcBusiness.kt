package id.co.awan.walle.business

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import id.co.awan.walle.service.dao.HSMService
import id.co.awan.walle.service.dao.MerchantService
import id.co.awan.walle.service.dao.TerminalService
import id.co.awan.walle.service.validation.EdcControllerValidation
import id.co.awan.walle.service.web3middleware.ERC20MiddlewareService
import id.co.awan.walle.service.web3middleware.EthMiddlewareService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class EdcBusiness(
    private val merchantService: MerchantService,
    private val hsmService: HSMService,
    private val ethMiddlewareService: EthMiddlewareService,
    private val erc20MiddlewareService: ERC20MiddlewareService,
    private val edcControllerValidation: EdcControllerValidation,
    private val terminalService: TerminalService
) {

    fun merchantInquiry(request: JsonNode): JsonNode {

        val (merchantId, merchantKey, terminalId, terminalKey) = edcControllerValidation.validateMerchantInquiry(request)

        val terminal = terminalService.validateTerminal(
            terminalId,
            terminalKey
        )

        val merchant = merchantService.validateMerchantWithTerminal(
            terminal,
            merchantId,
            merchantKey
        )

        val response = JsonNodeFactory.instance.objectNode().apply {
            put("merchantName", merchant.name)
            put("merchantAddress", merchant.address)
        }

        return response
    }

    fun paymentRequest(request: JsonNode): JsonNode {

        val (merchantId, merchantKey, terminalId, terminalKey, hashCard, hashPin, paymentAmount)
                = edcControllerValidation.validatePaymentRequest(request)

        // Validate Terminal
        val terminal = terminalService.validateTerminal(
            terminalId,
            terminalKey
        )

        // Validate Merchant
        val merchant = merchantService.validateMerchantWithTerminal(
            terminal,
            merchantId,
            merchantKey
        )


        // Find HSM
        val hsm = hsmService.getHsm(hashCard, hashPin)

        // Construct Json Response
        val response = JsonNodeFactory.instance.objectNode().apply {
            put(
                "fromAddress", hsm.walletProfile?.walletAddress ?: throw ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "01|hsm.ownerAddress should not be null"
                )
            )
            put("toAddress", merchant.address)
            put(
                "secretKey", hsm.secretKey ?: throw ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "01|hsm.secretKey should not be null"
                )
            )
        }

        return response
    }

    fun cardGassRecovery(request: JsonNode) {

        val (terminalId, terminalKey, merchantId, merchantKey, hashCard, hashPin, ethSignMessage, ownerAddress, cardAddress, chain)
                = edcControllerValidation.validateCardGassRecovery(request)

        // Validate Signature
        val message = String.format(
            "CARD_GASS_RECOVERY|%s|%s|%s|%s|%s|%s",
            hashCard,
            hashPin,
            merchantId,
            merchantKey,
            terminalId,
            terminalKey
        )

        ethMiddlewareService.validateEthSign(message, cardAddress, ethSignMessage)

        // Validate Terminal
        val terminal = terminalService.validateTerminal(
            terminalId,
            terminalKey
        )

        // Validate Merchant
        merchantService.validateMerchantWithTerminal(
            terminal,
            merchantId,
            merchantKey
        )

        // Validate Card is Valid Walle
        hsmService.getHsm(hashCard, hashPin, ownerAddress)

        // Try to recover card gas availability
        ethMiddlewareService.recoverCardGasAvailability(chain, cardAddress)
    }
}