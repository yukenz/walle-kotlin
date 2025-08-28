package id.co.awan.walle.service.dao

import id.co.awan.walle.entity.Chain
import id.co.awan.walle.entity.ERC20Metadata
import id.co.awan.walle.repository.Erc20MetadataRepository
import id.co.awan.walle.service.web3middleware.ERC20MiddlewareService
import org.springframework.stereotype.Service
import java.math.BigInteger

@Service
class Erc20MetadataService(
    private val erc20MetadataRepository: Erc20MetadataRepository,
    private val erc20MiddlewareService: ERC20MiddlewareService
) {

    fun getAllErc20ByChain(chain: Chain): Set<ERC20Metadata> = erc20MetadataRepository.findERC20MetadataByChain(chain)

    fun getAllErc20DetailsByChain(
        chain: Chain,
        walletAddress: String,
        cardAddress: String,
    ): Map<String, Array<BigInteger>> {
        // {string : [bigint, bigint]}
        // {erc20Address : [allowance, balanceOf]}
        return getAllErc20ByChain(chain)
            .associate { it -> // Loop ERC20 in Chain
                try {
                    val allowance = erc20MiddlewareService.allowance(
                        chain.chainName,
                        it.address,
                        walletAddress,
                        cardAddress,
                    )
                    val balanceOf = erc20MiddlewareService.balanceOf(
                        chain.chainName,
                        it.address,
                        walletAddress
                    )
                    it.address to arrayOf(allowance, balanceOf)
                } catch (e: Exception) {
                    it.address to emptyArray()
                }
            }
    }
}