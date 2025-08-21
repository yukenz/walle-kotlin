package id.co.awan.walle.repository

import id.co.awan.walle.entity.Hsm
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface HsmRepository : JpaRepository<Hsm, String> {

    fun existsByOwnerAddress(ownerAddress: String): Boolean

    fun findAllByOwnerAddress(ownerAddress: String): MutableList<Hsm>

    fun findByIdAndPinAndOwnerAddress(id: String, pin: String, ownerAddress: String): Hsm?

    fun findByIdAndPin(id: String, pin: String): Hsm?

}