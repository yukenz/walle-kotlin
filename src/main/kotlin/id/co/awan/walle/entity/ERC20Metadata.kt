package id.co.awan.walle.entity

import jakarta.persistence.*

@Entity
@Table(name = "erc20_metadata", schema = "tap2pay")
class ERC20Metadata {

    @Id
    @Column(name = "id")
    lateinit var address: String

    @Column(name = "token_symbol")
    lateinit var tokenSymbol: String;

    @Column(name = "token_name")
    lateinit var tokenName: String;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chain_id")
    var chain: Chain? = null

}