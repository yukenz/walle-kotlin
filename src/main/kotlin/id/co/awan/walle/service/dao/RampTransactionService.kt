package id.co.awan.walle.service.dao

import id.co.awan.walle.entity.OnrampTransaction
import id.co.awan.walle.repository.OnrampTransactionRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.math.BigInteger

@Service
class RampTransactionService(
    private val onrampTransactionRepository: OnrampTransactionRepository
) {

    // ========================================================
    // CREATE TRANSACTION SECTION
    // ========================================================
    @Transactional
    fun createTransactionOnRampFirstPhase(
        walletAddress: String,
        chain: String,
        erc20Address: String,
        amount: BigInteger
    ): String {

        // Pastikan tidak ada pending trx
        if (inquiryByWalletAddress(walletAddress) != null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Please complete previous transaction")
        }

        val onrampTransaction: OnrampTransaction = OnrampTransaction().apply {
            this.walletAddress = walletAddress
            this.chain = chain
            this.erc20Address = erc20Address
            this.grossAmount = amount
        }

        onrampTransactionRepository.save(onrampTransaction)
        return onrampTransaction.orderId
    }

    @Transactional
    fun createTransactionOnRampSecondPhase(
        orderId: String,
        redirectUrl: String,
        token: String
    ) {
        val onrampTransaction: OnrampTransaction = inquiryByOrderId(orderId).apply {
            this.redirectUrl = (redirectUrl)
            this.token = (token)
            this.transactionStatus = "created"
        }
        onrampTransactionRepository.save(onrampTransaction)
    }

    @Transactional
    fun errorTransactionOnRampSecondPhase(
        orderId: String,
        error: String
    ) {

        val onrampTransaction = inquiryByOrderId(orderId)

        // Sempurnakan TRX
        onrampTransaction.transactionStatus = "error"
        onrampTransaction.errorCause = error
        onrampTransactionRepository.save(onrampTransaction)
    }


    // ========================================================
    // UPDATE TRANSACTION SECTION
    // ========================================================
    @Transactional
    fun updateTransactionOnRamp(
        entity: OnrampTransaction
    ): OnrampTransaction {
        val onrampTransaction = inquiryByOrderId(entity.orderId)

        // Prohibited Double Update
        if (onrampTransaction.transactionStatus.equals(entity.transactionStatus, ignoreCase = true)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Transaction status already set")
        }

        // Update NOTIFICATION
        onrampTransaction.apply {
            this.transactionStatus = entity.transactionStatus
            this.transactionId = entity.transactionId
            this.settlementTime = entity.settlementTime
            this.paymentType = entity.paymentType
            this.fraudStatus = entity.fraudStatus
            this.currency = entity.currency
            this.onchainReceipt = entity.onchainReceipt
        }
        onrampTransactionRepository.save(onrampTransaction)

        return onrampTransaction
    }

    // ========================================================
    // NON TRANSACTIONAL
    // ========================================================

    fun inquiryTransactionReceipt(orderId: String): String? {
        return onrampTransactionRepository
            .findTransactionReceiptById(orderId)
    }

    fun inquiryByOrderId(orderId: String): OnrampTransaction {
        return onrampTransactionRepository
            .findById(orderId)
            .orElseThrow({ ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found") })
    }

    fun inquiryByWalletAddress(walletAddress: String): OnrampTransaction? {
        return onrampTransactionRepository
            .findByWalletAddressAndTransactionStatus(walletAddress, "pending")
    }

}