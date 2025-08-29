package id.co.awan.walle.service.dao

import id.co.awan.walle.entity.Hsm
import id.co.awan.walle.entity.WalletProfile
import id.co.awan.walle.repository.HsmRepository
import id.co.awan.walle.repository.WalletProfileRepository
import org.apache.hc.client5.http.utils.Hex
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.security.SecureRandom

@Service
class HSMService(
    private val hsmRepository: HsmRepository,
    private val walletProfileRepository: WalletProfileRepository
) {

    @Transactional
    fun changePin(
        hashCard: String,
        hashPin: String,
        newHashPin: String,
        ownerAddress: String
    ) {
        val hsm: Hsm = getHsm(hashCard, hashPin, ownerAddress)
        hsm.pin = newHashPin
        hsmRepository.save(hsm)
    }


    fun getHsm(
        hashCard: String,
        hashPin: String,
        ownerAddress: String
    ): Hsm {

        return hsmRepository.findByHashCardAndPinAndWalletProfile(
            id = hashCard,
            pin = hashPin,
            walletProfile = WalletProfile().apply {
                walletAddress = ownerAddress
            }
        ) ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND, "HSM Not Found"
        )
    }

    fun getHsm(
        hashCard: String,
        hashPin: String
    ): Hsm = hsmRepository.findByHashCardAndPin(hashCard, hashPin)
        ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND, "HSM Not Found"
        )

    fun getHsm(
        hashCard: String,
    ): Hsm = hsmRepository.findById(hashCard)
        .orElseThrow {
            ResponseStatusException(
                HttpStatus.NOT_FOUND, "HSM Not Found"
            )
        }

    @Transactional
    fun resetHsm(
        hashCardUUID: String
    ) {

        val hsm = hsmRepository.findById(hashCardUUID)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "01|HSM not found") }!!

        hsm.pin = null
        hsm.secretKey = null
        hsm.walletProfile = null

        hsmRepository.save(hsm)
    }


    @Transactional
    fun createCard(
        hashCard: String,
        hashPin: String,
        ownerAddress: String
    ) {

        val hsm = hsmRepository.findById(hashCard)
            .orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "Card UUID not valid") }

        // Cord already registered with some address
        if (hsm.walletProfile?.walletAddress != null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Card already registered")
        }

        val walletProfile = walletProfileRepository.findById(ownerAddress.lowercase())
            .orElse(WalletProfile().apply {
                walletAddress = ownerAddress.lowercase()
                username = "changethisusername"
                email = "changethisemail"
            })

        walletProfileRepository.save(walletProfile)

        hsm.pin = hashPin
        hsm.secretKey = Hex.encodeHexString(SecureRandom.getInstanceStrong().generateSeed(32))
        hsm.walletProfile = walletProfile

        hsmRepository.save(hsm)
    }

    fun getCard(ownerAddress: String): MutableList<String> {
        val userProfile = walletProfileRepository.findById(ownerAddress.lowercase())
        return userProfile.get().hsm
            .map { it.hashCard }   // Kotlin way - maps and filters nulls
            .toMutableList()        // Convert to MutableList)
    }

    fun getWalletOwnerByHashCard(hashCard: String): String {
        return hsmRepository.findById(hashCard.lowercase())
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "HSM Not Found") }
            .walletProfile?.walletAddress
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Owner Not Found")
    }
}

