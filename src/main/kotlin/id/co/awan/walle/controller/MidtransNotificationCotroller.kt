package id.co.awan.walle.controller

import com.fasterxml.jackson.databind.JsonNode
import id.co.awan.walle.constant.MidtransTransactionStatus.*
import id.co.awan.walle.entity.OnrampTransaction
import id.co.awan.walle.service.ERC20MiddlewareService
import id.co.awan.walle.service.MidtransNotificationService
import id.co.awan.walle.service.RampTransactionService
import id.co.awan.walle.utils.LogUtils
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@RestController
@RequestMapping("/api/midtrans/notification")
class MidtransNotificationCotroller(
    private val midtransNotificationService: MidtransNotificationService,
    private val rampTransactionService: RampTransactionService,
    private val eRC20MiddlewareService: ERC20MiddlewareService
) {

    private val log = LoggerFactory.getLogger(this::class.java)

    @PostMapping(
        path = ["/payment"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @Throws(NoSuchAlgorithmException::class, NoSuchProviderException::class)
    fun paymentNotification(
        @RequestBody request: JsonNode
    ): ResponseEntity<String?> {

        val reqToken = LogUtils.logHttpRequest(this.javaClass, "paymentNotification", request)

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

        // Force NonNull for advance checking
        val transactionReceipt = rampTransactionService.inquiryTransactionReceipt(orderId)
        if (transactionReceipt == null) {
            if (transactionStatusEnum == CAPTURE || transactionStatusEnum == SETTLEMENT) {
                val onrampTransaction: OnrampTransaction = rampTransactionService.inquiryByOrderId(orderId)
                onchainReceipt = eRC20MiddlewareService.transfer(
                    onrampTransaction.chain,
                    onrampTransaction.erc20Address,
                    onrampTransaction.walletAddress,  // TODO: Working on decimals
                    onrampTransaction.grossAmount.toString(),
                    ERC20MiddlewareService.ScOperation.WRITE
                )
            }
        } else {
            onchainReceipt = transactionReceipt
        }

        // ========================================================
        // UPDATE TRANSACTION SECTION
        // ========================================================
        var settlementTimeDateTime: LocalDateTime? = null
        if (settlementTime != null) {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            settlementTimeDateTime = LocalDateTime.parse(settlementTime, formatter)
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

        return ResponseEntity.ok<String?>(null)
    }
}