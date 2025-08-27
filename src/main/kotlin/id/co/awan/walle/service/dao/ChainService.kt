package id.co.awan.walle.service.dao

import id.co.awan.walle.entity.Chain
import id.co.awan.walle.entity.ERC20Metadata
import id.co.awan.walle.repository.ChainRepository
import org.springframework.stereotype.Service

@Service
class ChainService(
    private val chainRepository: ChainRepository
) {

    fun getAllERC20Token(chainId: String): Set<ERC20Metadata> {
        return chainRepository.findErc20ListByChainName(chainId);
    }

    fun getAllRegisteredChain(): Set<Chain> = chainRepository.findAll().toSet()

}