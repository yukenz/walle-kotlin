package id.co.awan.walle.business

import com.fasterxml.jackson.databind.JsonNode
import id.co.awan.walle.constant.CardSelfServiceOperation
import id.co.awan.walle.service.dao.HSMService
import id.co.awan.walle.service.validation.CardControllerValidation
import id.co.awan.walle.service.web3middleware.Eip712MiddlewareService
import id.co.awan.walle.service.web3middleware.EthMiddlewareService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class CardBusiness(
    private val validator: CardControllerValidation,
    private val eip712MiddlewareService: Eip712MiddlewareService,
    private val hsmService: HSMService,
    private val ethMiddlewareService: EthMiddlewareService
) {

    fun registerCard(ethSignMessage: String): MutableList<String> {
        // Validate Signature
        val addressRecovered = ethMiddlewareService.ecRecover("CARD_QUERY", ethSignMessage)

        // Query Cards
        val cards = hsmService.getCard(addressRecovered)

        return cards
    }

    fun registerCard(request: JsonNode): Unit {

        // Destruct Request
        val (chain, hashCard, hashPin, ethSignMessage, signerAddress) = validator.validateRegisterCard(request)

        // Validate Signature
        val recoveredAddress = eip712MiddlewareService.validateSignerCardSelfService(
            chain,
            CardSelfServiceOperation.REGISER,
            hashCard,
            hashPin,
            ethSignMessage,
            signerAddress
        )

        val hsm = hsmService.getHsm(recoveredAddress)

        // Cord already registered with some address
        if (hsm.walletProfile?.walletAddress != null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Card already registered")
        }

        // Create Card
        hsmService.createCard(
            hashCard, hashPin,
            ownerAddress = recoveredAddress
        )

    }

    fun accessCard(request: JsonNode): String? {

        // Destruct Request
        val (chain, hashCard, hashPin, ethSignMessage, signerAddress) = validator.validateAccessCard(request)

        // Validate Signature
        val recoveredAddress = eip712MiddlewareService.validateSignerCardSelfService(
            chain,
            CardSelfServiceOperation.ACCESS,
            hashCard,
            hashPin,
            ethSignMessage,
            signerAddress
        )

        // Find HSM
        val hsm = hsmService.getHsm(
            hashCard,
            hashPin,
            recoveredAddress
        )

        return hsm.secretKey
    }

    fun changePin(request: JsonNode): Unit {
        // Destruct Request
        val (chain, hashCard, hashPin, ethSignMessage, newHashPin, signerAddress) = validator.validateChangePin(request)

        // Validate Signature
        val recoveredAddress = eip712MiddlewareService.validateSignerCardSelfService(
            chain,
            CardSelfServiceOperation.CHANGE_PIN,
            hashCard,
            hashPin,
            ethSignMessage,
            signerAddress
        )

        // Change PIN
        hsmService.changePin(
            hashCard,
            hashPin,
            newHashPin,
            recoveredAddress
        )

    }


}