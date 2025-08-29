package id.co.awan.walle.repository

import id.co.awan.walle.entity.WalletProfile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserProfileRepository : JpaRepository<WalletProfile, String> {


}