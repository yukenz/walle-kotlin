package id.co.awan.walle.service.dao

import id.co.awan.walle.entity.Hsm
import id.co.awan.walle.repository.HsmRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class HSMService(
    private val hsmRepository: HsmRepository
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

        return hsmRepository.findByIdAndPinAndOwnerAddress(
            id = hashCard,
            pin = hashPin,
            ownerAddress.lowercase()
        ) ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND, "HSM Not Found"
        )
    }

    fun getHsm(
        hashCard: String,
        hashPin: String
    ): Hsm {

        return hsmRepository.findByIdAndPin(hashCard, hashPin)
            ?: throw ResponseStatusException(
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
        hsm.ownerAddress = null
        hsm.secretKey = null

        hsmRepository.save(hsm)
    }

}