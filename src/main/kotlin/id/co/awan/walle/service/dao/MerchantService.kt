package id.co.awan.walle.service.dao

import id.co.awan.walle.entity.Merchant
import id.co.awan.walle.entity.Terminal
import id.co.awan.walle.repository.HsmRepository
import id.co.awan.walle.repository.MerchantRepository
import id.co.awan.walle.repository.TerminalRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class MerchantService(
    private val hsmRepository: HsmRepository,
    private val merchantRepository: MerchantRepository,
    private val terminalRepository: TerminalRepository
) {


    fun validateMerchant(
        merchantId: String,
    ): Merchant {

        return merchantRepository.findById(merchantId).orElseThrow {
            ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "Merchant ID isn't valid"
            )
        }
    }

    fun validateMerchantWithTerminal(
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
}