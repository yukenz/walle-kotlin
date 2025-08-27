package id.co.awan.walle.controller

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import id.co.awan.walle.service.dao.ChainService
import id.co.awan.walle.service.dao.Erc20MetadataService
import id.co.awan.walle.service.dao.HSMService
import id.co.awan.walle.service.dao.MerchantService
import id.co.awan.walle.service.validation.QrControllerValidation
import id.co.awan.walle.service.web3middleware.ERC20MiddlewareService
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/v1/qr")
class QrController(
    private val merchantService: MerchantService,
    private val qrControllerValidation: QrControllerValidation,
    private val hsmService: HSMService,
    private val chainService: ChainService,
    private val erc20MetadataService: Erc20MetadataService,
    private val eRC20MiddlewareService: ERC20MiddlewareService
) {

    @Operation(summary = "Query QR Merchant")
    @GetMapping(
        path = ["/query"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun qrInquiry(
        @RequestParam(name = "merchantId") merchantId: String,
    ): ResponseEntity<Any?> {

        val merchant = merchantService.validateMerchant(merchantId)

        val response = JsonNodeFactory.instance.objectNode().apply {
            put("id", merchant.id)
            put("name", merchant.name)
            put("address", merchant.address)
        }

        return ResponseEntity.ok(response)
    }

    @Operation(summary = "Query QR Merchant")
    @GetMapping(
        path = ["/metadata-inquiry"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun qrMetadataInquiry(
        @RequestParam(name = "cardAddress") cardAddress: String,
        @RequestParam(name = "hashCard") hashCard: String,
    ): ResponseEntity<*> {

        val walletAddress = hsmService.getOwnerById(hashCard)

        val response = chainService.getAllRegisteredChain()
            .associate { it1 -> // Loop chain
                /* return it1 */ try {
                    val erc20Details = erc20MetadataService.getAllErc20ByChain(it1)
                        .associate { it2 -> // Loop ERC20 in Chain
                            /* return it2 */ try {
                                val allowance = eRC20MiddlewareService.allowance(
                                    it1.chainName,
                                    it2.address,
                                    walletAddress,
                                    cardAddress,
                                )
                                val balanceOf = eRC20MiddlewareService.balanceOf(
                                    it1.chainName,
                                    it2.address,
                                    walletAddress
                                )
                                it2.address to arrayOf(allowance, balanceOf)
                            } catch (e: Exception) {
                                it2.address to emptyArray()
                            }
                        }
                    it1.chainName to erc20Details
                } catch (e: Exception) {
                    it1.chainName to emptyMap()
                }
            }

        return ResponseEntity.ok(response)
    }


    @Operation(summary = "Payment QR Merchant")
    @PostMapping(
        path = ["/payment"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.TEXT_PLAIN_VALUE]
    )
    fun qrPayment(
        @RequestBody request: JsonNode
    ): ResponseEntity<Any?> {

        val (merchantId, cardAddress, amount, chain, erc20Address, hashCard, hashPin, ethSignMessage)
                = qrControllerValidation.validateQrPayment(request)


// TODO: Diskusi chain Id or Name
        val findAllERC20Token = chainService.getAllERC20Token(chain)


// TODO: Implement Payment QR
// 1. Checking allowance
// 2. Sponsor / Recovery Card Gass
// 3. TransferFrom

        return ResponseEntity.ok().body(null);

    }


}