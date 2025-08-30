package id.co.awan.walle.business

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import id.co.awan.walle.service.dao.ChainService
import id.co.awan.walle.service.dao.Erc20MetadataService
import id.co.awan.walle.service.dao.HSMService
import id.co.awan.walle.service.dao.MerchantService
import id.co.awan.walle.service.validation.QrControllerValidation
import id.co.awan.walle.service.web3middleware.ERC20MiddlewareService
import id.co.awan.walle.service.web3middleware.EthMiddlewareService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.math.BigInteger
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

@Service
class QrBusiness(
    private val merchantService: MerchantService,
    private val qrControllerValidation: QrControllerValidation,
    private val hsmService: HSMService,
    private val chainService: ChainService,
    private val erc20MetadataService: Erc20MetadataService,
    private val erc20MiddlewareService: ERC20MiddlewareService,
    private val ethMiddlewareService: EthMiddlewareService,
) {

    private val log = LoggerFactory.getLogger(this::class.java)

    fun qrInquiry(merchantId: String): ObjectNode? {

        val merchant = merchantService.validateMerchant(merchantId)

        val response = JsonNodeFactory.instance.objectNode().apply {
            put("id", merchant.id)
            put("name", merchant.name)
        }

        return response
    }

    fun qrMetadataInquiry(cardAddress: String, hashCard: String): Map<String, Map<String, Array<BigInteger>>> {

        val walletAddress = hsmService.getWalletOwnerByHashCard(hashCard)
        val response = chainService.getAllRegisteredChain()
            .associate {
                try {
                    val allErc20DetailsByChain =
                        erc20MetadataService.getAllErc20DetailsByChain(it, walletAddress, cardAddress)
                    it.chainName to allErc20DetailsByChain
                } catch (e: Exception) {
                    it.chainName to emptyMap()
                }
            }

        return response
    }

    fun qrPayment(request: JsonNode): List<String> {

        val (merchantId, cardAddress, amount, chain, erc20Address, hashCard, hashPin, ethSignMessage)
                = qrControllerValidation.validateQrPayment(request)

        // Validate Signature
        ethMiddlewareService.validateEthSign(
            "QR_PAYMENT|$merchantId|$chain|$amount|$hashCard|$hashPin",
            cardAddress,
            ethSignMessage
        )

        // Init threadpool
        val executorService = Executors.newFixedThreadPool(3) { runnable ->
            Thread(runnable, "qrpayment")
        }
        // Find HSM
        val deferredHsm = CompletableFuture.supplyAsync({ hsmService.validateHsm(hashCard, hashPin) }, executorService)
        // Find Merchant
        val deferredMerchant =
            CompletableFuture.supplyAsync({ merchantService.validateMerchant(merchantId) }, executorService)
        // Find Gas Price
        val deferredGasFee =
            CompletableFuture.supplyAsync(
                { ethMiddlewareService.gasPrice(chain).multiply(BigInteger.valueOf(70000L)) },
                executorService
            )
        // Get Card Balance
        val deferredCardGasBalance =
            CompletableFuture.supplyAsync({ ethMiddlewareService.balanceOf(chain, cardAddress) }, executorService)

        // Await
        CompletableFuture.allOf(deferredHsm, deferredMerchant, deferredGasFee, deferredCardGasBalance).join()
        val hsm = deferredHsm.get()
        val merchant = deferredMerchant.get()
        val gasFee = deferredGasFee.get()
        val cardGasBalance = deferredCardGasBalance.get()
        executorService.shutdown()

        // Validate Allowance
        erc20MiddlewareService.allowance(
            chain, erc20Address,
            sourceAddress = hsm.walletProfile!!.walletAddress,
            destinationAddress = merchant.address
        ).run {
            if (this < amount) {
                ResponseStatusException(
                    HttpStatus.FORBIDDEN, "allowance not enought"
                )
            }
        }

        // Gas Recover
        if (cardGasBalance < gasFee) {
            val amountForRecover = gasFee - cardGasBalance
            val transactionReceipt = ethMiddlewareService.transferEIP1559(chain, cardAddress, amountForRecover, "WEI")
            log.info(
                "Gas fee recover from card adress {} with amount [{} wei], trx id[{}]",
                cardAddress,
                amountForRecover,
                transactionReceipt
            )
        } else {
            log.info(
                "Gas fee is safety state for card address: {} with balance {}",
                cardAddress,
                cardGasBalance
            )
        }

        return listOf(merchant.address, hsm.secretKey!!)

    }
}