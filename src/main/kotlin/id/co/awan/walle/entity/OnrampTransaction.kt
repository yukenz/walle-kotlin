package id.co.awan.walle.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Generated
import org.hibernate.generator.EventType
import java.math.BigInteger
import java.time.LocalDateTime

@Entity
@Table(name = "onramp", schema = "walle")
class OnrampTransaction {

    /* =================================================================================================================
     * CREATIONAL
     * =================================================================================================================
     */

    @Id
    @Column(name = "id", updatable = false, nullable = false, insertable = false)
    @Generated(event = [EventType.INSERT])
    lateinit var orderId: String

    @Column(name = "gross_amount", nullable = false)
    lateinit var grossAmount: BigInteger

    @Column(name = "wallet_address", nullable = false)
    lateinit var walletAddress: String

    @Column(name = "chain", nullable = false)
    lateinit var chain: String

    @Column(name = "erc20_address", nullable = false)
    lateinit var erc20Address: String

    @Column(name = "redirect_url", nullable = true)
    var redirectUrl: String? = null

    @Column(name = "token", nullable = true)
    var token: String? = null

    @Column(name = "error_cause", nullable = true)
    var errorCause: String? = null

    /* =================================================================================================================
     * NOTIFICATION
     * =================================================================================================================
     */

    @Column(name = "transaction_status", nullable = true)
    var transactionStatus: String? = null

    @Column(name = "transaction_id", nullable = true)
    var transactionId: String? = null

    @Column(name = "settlement_time", nullable = true)
    var settlementTime: LocalDateTime? = null

    @Column(name = "payment_type", nullable = true)
    var paymentType: String? = null

    @Column(name = "fraud_status", nullable = true)
    var fraudStatus: String? = null

    @Column(name = "currency", nullable = true)
    var currency: String? = null

    @Column(name = "onchain_receipt", nullable = true)
    var onchainReceipt: String? = null

}