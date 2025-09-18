package id.co.awan.walle.entity

import jakarta.persistence.*

@Entity
@Table(name = "chain", schema = "walle")
class Chain {

    @Id
    @Column(name = "id")
    lateinit var id: String

    @Column(name = "name")
    lateinit var chainName: String;

    @Column(name = "token_symbol")
    lateinit var tokenSymbol: String;

    @Column(name = "token_name")
    lateinit var tokenName: String;

    @OneToMany(mappedBy = "chain")
    var erc20Metadata: Set<ERC20Metadata> = emptySet()

}