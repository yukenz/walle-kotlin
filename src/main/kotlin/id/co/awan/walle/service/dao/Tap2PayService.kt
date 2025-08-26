package id.co.awan.walle.service.dao

import id.co.awan.walle.entity.Hsm
import id.co.awan.walle.entity.Merchant
import id.co.awan.walle.entity.Terminal
import id.co.awan.walle.repository.HsmRepository
import id.co.awan.walle.repository.TerminalRepository
import org.apache.hc.client5.http.utils.Hex
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.security.SecureRandom
import java.util.*

@Service
class Tap2PayService(
    private val hsmRepository: HsmRepository,
    private val terminalRepository: TerminalRepository
) {

    fun validateMerchant(
        terminal: Terminal,
        merchantId: String,
        merchantKey: String
    ): Merchant {

        val merchant: Merchant = terminal.merchant
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND, "01|Terminal not linked to merchant"
            )

        if (!merchantId.equals(merchant.id, ignoreCase = false)) {
            throw ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "Merchant ID for this Terminal isn't valid"
            )
        }

        if (!merchantKey.equals(merchant.key, ignoreCase = false)) {
            throw ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "Key for merchant ID isn't valid"
            )
        }

        return merchant
    }

    fun validateTerminal(
        terminalId: String,
        terminalKey: String
    ): Terminal {
        return terminalRepository.findByIdAndKey(terminalId, terminalKey)
            ?: throw ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "Terminal validation exception"
            )
    }

    @Transactional
    fun createCard(
        hashCard: String,
        hashPin: String,
        ownerAddress: String
    ) {

        val hsmResult: Optional<Hsm> = hsmRepository.findById(hashCard)
        // Cord not issued
        if (hsmResult.isEmpty) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Card UUID not valid")
        }

        val hsm: Hsm = hsmResult.get()

        // Cord already registered with some address
        if (hsm.ownerAddress != null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Card already registered")
        }

        hsm.ownerAddress = ownerAddress.lowercase(Locale.getDefault())
        hsm.pin = hashPin
        hsm.secretKey = Hex.encodeHexString(SecureRandom.getInstanceStrong().generateSeed(32))

        hsmRepository.save(hsm)
    }

    fun getCards(ownerAddress: String): MutableList<String> {
        return hsmRepository.findAllByOwnerAddress(ownerAddress.lowercase())
            .mapNotNull { it.id }   // Kotlin way - maps and filters nulls
            .toMutableList()        // Convert to MutableList)
    }
}