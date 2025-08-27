package id.co.awan.walle.repository

import id.co.awan.walle.entity.Chain
import id.co.awan.walle.entity.ERC20Metadata
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface Erc20MetadataRepository : JpaRepository<ERC20Metadata, String> {

    fun findERC20MetadataByChain(chain: Chain): Set<ERC20Metadata>

}