package id.co.awan.walle.repository

import id.co.awan.walle.entity.Merchant
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MerchantRepository : JpaRepository<Merchant, String> {
}