package id.co.awan.walle.repository

import id.co.awan.walle.entity.Chain
import id.co.awan.walle.entity.ERC20Metadata
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ChainRepository : JpaRepository<Chain, String> {

    @Query("SELECT e.erc20Metadata FROM Chain e WHERE e.id = :chainId")
    fun findErc20ListByChainName(@Param("chainId") orderId: String): Set<ERC20Metadata>

}