package id.co.awan.walle.repository

import id.co.awan.walle.entity.OnrampTransaction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface OnrampTransactionRepository : JpaRepository<OnrampTransaction, String> {

    fun findByWalletAddressAndTransactionStatus(walletAddress: String, transactionStatus: String): OnrampTransaction?

    fun findAllByWalletAddress(walletAddress: String): MutableList<OnrampTransaction>

    @Query("SELECT e.onchainReceipt FROM OnrampTransaction e WHERE e.orderId = :orderId")
    fun findTransactionReceiptById(@Param("orderId") orderId: String): String?
}