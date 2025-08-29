package id.co.awan.walle.business

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import id.co.awan.walle.service.dao.ChainService
import id.co.awan.walle.service.dao.Erc20MetadataService
import id.co.awan.walle.service.dao.HSMService
import id.co.awan.walle.service.dao.MerchantService
import id.co.awan.walle.service.validation.QrControllerValidation
import org.springframework.stereotype.Service
import java.math.BigInteger

@Service
class QrBusiness(
    private val merchantService: MerchantService,
    private val qrControllerValidation: QrControllerValidation,
    private val hsmService: HSMService,
    private val chainService: ChainService,
    private val erc20MetadataService: Erc20MetadataService,
) {

    fun qrInquiry(merchantId: String, cardAddress: String): ObjectNode? {

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

    fun qrPayment(request: JsonNode) {

        val (merchantId, cardAddress, amount, chain, erc20Address, hashCard, hashPin, ethSignMessage)
                = qrControllerValidation.validateQrPayment(request)


        // TODO: Diskusi chain Id or Name
        val findAllERC20Token = chainService.getAllERC20Token(chain)


        // TODO: Implement Payment QR
        // 1. Checking allowance
        // 2. Sponsor / Recovery Card Gass
        // 3. TransferFrom

    }
}