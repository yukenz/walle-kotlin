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
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import org.springframework.boot.LazyInitializationExcludeFilter
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.math.BigInteger

@Service
class QrBusiness(
    private val merchantService: MerchantService,
    private val qrControllerValidation: QrControllerValidation,
    private val hsmService: HSMService,
    private val chainService: ChainService,
    private val erc20MetadataService: Erc20MetadataService,
    private val erc20MiddlewareService: ERC20MiddlewareService,
    private val scheduledBeanLazyInitializationExcludeFilter: LazyInitializationExcludeFilter,
    private val ethMiddlewareService: EthMiddlewareService,
) {

    private val log = LoggerFactory.getLogger(this::class.java)

    fun qrInquiry(merchantId: String): ObjectNode? {

        val merchant = merchantService.validateMerchant(merchantId)

        val response = JsonNodeFactory.instance.objectNode().apply {
            put("id", merchant.id)
            put("name", merchant.name)
            put("address", merchant.address)
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

    suspend fun qrPayment(request: JsonNode): Unit = coroutineScope {

        val (merchantId, cardAddress, amount, chain, erc20Address, hashCard, hashPin, ethSignMessage)
                = qrControllerValidation.validateQrPayment(request)

        // Validate Signature
        ethMiddlewareService.validateEthSign(
            "QR_PAYMENT|$merchantId|$chain|$amount|$hashCard|$hashPin",
            cardAddress,
            ethSignMessage
        )
        // Find HSM
        val deferredHsm = async { hsmService.validateHsm(hashCard, hashPin) }
        // Find Merchant
        val deferredMerchant = async { merchantService.validateMerchant(merchantId) }
        // Find Gas Price
        val deferredGasFee = async { ethMiddlewareService.gasPrice(chain).multiply(BigInteger.valueOf(70000L)) }
        // Get Card Balance
        val deferredCardGasBalance = async { ethMiddlewareService.balanceOf(chain, cardAddress) }

        // Await
        val hsm = deferredHsm.await()
        val merchant = deferredMerchant.await()
        val gasFee = deferredGasFee.await()
        val cardGasBalance = deferredCardGasBalance.await()

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

    }
}