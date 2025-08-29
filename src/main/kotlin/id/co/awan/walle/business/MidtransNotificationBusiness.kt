package id.co.awan.walle.business

import com.fasterxml.jackson.databind.JsonNode
import id.co.awan.walle.constant.MidtransTransactionStatus.*
import id.co.awan.walle.entity.OnrampTransaction
import id.co.awan.walle.service.dao.RampTransactionService
import id.co.awan.walle.service.midtrans.MidtransNotificationService
import id.co.awan.walle.service.web3middleware.ERC20MiddlewareService
import id.co.awan.walle.utils.LogUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class MidtransNotificationBusiness(
    private val midtransNotificationService: MidtransNotificationService,
    private val rampTransactionService: RampTransactionService,
    private val eRC20MiddlewareService: ERC20MiddlewareService
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    fun paymentNotification(request: JsonNode): Unit {

        LogUtils.logHttpRequest(this.javaClass, "paymentNotification", request)

        midtransNotificationService.validateSignature(request)

        val orderId = request.at("/order_id").asText(null)
        val transactionStatus = request.at("/transaction_status").asText(null)
        val transactionId = request.at("/transaction_id").asText(null)
        val settlementTime = request.at("/settlement_time").asText(null)
        val paymentType = request.at("/payment_type").asText(null)
        val fraudStatus = request.at("/fraud_status").asText(null)
        val currency = request.at("/currency").asText(null)

        val transactionStatusEnum = valueOf(transactionStatus.uppercase(Locale.getDefault()))

        when (transactionStatusEnum) {
            PENDING -> log.info("{}", PENDING)
            CAPTURE -> log.info("{}", CAPTURE)
            SETTLEMENT -> log.info("{}", SETTLEMENT)
            REFUND -> log.info("{}", REFUND)
            CANCEL -> log.info("{}", CANCEL)
            DENY -> log.info("{}", DENY)
            EXPIRE -> log.info("{}", EXPIRE)
            FAILURE -> log.info("{}", FAILURE)
            CHARGEBACK -> log.info("{}", CHARGEBACK)
            PARTIAL_REFUND -> log.info("{}", PARTIAL_REFUND)
            PARTIAL_CHARGEBACK -> log.info("{}", PARTIAL_CHARGEBACK)
            AUTHORIZE -> log.info("{}", AUTHORIZE)
        }

//        try {
//            when (request.at("/payment_type").asText()) {
//                "credit_card" -> midtransNotificationService.creditCardProcessPayment(request)
//                "gopay" -> midtransNotificationService.gopayProcessPayment(request)
//                "qris" -> midtransNotificationService.qrisProcessPayment(request)
//                "shopeepay" -> midtransNotificationService.shopeepayProcessPayment(request)
//                "bank_transfer" -> midtransNotificationService.bankTransferProcessPayment(request)
//                "echannel" -> midtransNotificationService.echannelProcessPayment(request)
//                "cstore" -> midtransNotificationService.cstoreProcessPayment(request)
//                "akulaku" -> midtransNotificationService.akulakuProcessPayment(request)
//                else -> midtransNotificationService.defaultProcessPayment(request)
//            }
//        } catch (e: Exception) {
//            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message)
//        }

        // ========================================================
        // SEND TOKEN
        // ========================================================
        var onchainReceipt: String? = null
        rampTransactionService.inquiryTransactionReceipt(orderId).also {
            if (it == null) {
                if ((transactionStatusEnum == CAPTURE) or (transactionStatusEnum == SETTLEMENT)) {

                    val onrampTransaction = rampTransactionService.inquiryByOrderId(orderId)

                    val amountErc20Parsed = eRC20MiddlewareService.parseAmountDecimal(
                        onrampTransaction.chain,
                        onrampTransaction.grossAmount,
                        onrampTransaction.erc20Address
                    )

                    onchainReceipt = eRC20MiddlewareService.transfer(
                        onrampTransaction.chain,
                        onrampTransaction.erc20Address,
                        onrampTransaction.walletAddress,
                        amountErc20Parsed.toString(),
                        ERC20MiddlewareService.ScOperation.WRITE
                    )
                }
            } else {
                onchainReceipt = it
            }
        }

        // ========================================================
        // UPDATE TRANSACTION SECTION
        // ========================================================
        var settlementTimeDateTime: LocalDateTime? = null
        settlementTime.also {
            if (it != null) {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                settlementTimeDateTime = LocalDateTime.parse(it, formatter)
            }
        }

        rampTransactionService.updateTransactionOnRamp(
            OnrampTransaction().apply
            {
                this.orderId = orderId
                this.transactionStatus = transactionStatus
                this.transactionId = transactionId
                this.settlementTime = settlementTimeDateTime
                this.paymentType = paymentType
                this.fraudStatus = fraudStatus
                this.currency = currency
                this.onchainReceipt = onchainReceipt
            }
        )

    }
}