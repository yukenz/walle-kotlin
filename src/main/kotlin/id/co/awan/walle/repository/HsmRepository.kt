package id.co.awan.walle.repository

import id.co.awan.walle.entity.Hsm
import id.co.awan.walle.entity.WalletProfile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface HsmRepository : JpaRepository<Hsm, String> {

    fun findByHashCardAndPin(id: String, pin: String): Hsm?
    fun findByHashCardAndPinAndWalletProfile(id: String, pin: String, walletProfile: WalletProfile): Hsm?

}