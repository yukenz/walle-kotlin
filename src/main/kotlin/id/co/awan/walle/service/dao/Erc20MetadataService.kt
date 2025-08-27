package id.co.awan.walle.service.dao

import id.co.awan.walle.entity.Chain
import id.co.awan.walle.entity.ERC20Metadata
import id.co.awan.walle.repository.Erc20MetadataRepository
import org.springframework.stereotype.Service

@Service
class Erc20MetadataService(
    private val erc20MetadataRepository: Erc20MetadataRepository
) {

    fun getAllErc20ByChain(chain: Chain): Set<ERC20Metadata> = erc20MetadataRepository.findERC20MetadataByChain(chain)

}