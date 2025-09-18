package id.co.awan.walle.entity

import jakarta.persistence.*

@Entity
@Table(name = "hsm", schema = "walle")
class Hsm {

    @Id
    @Column(name = "hash_card", nullable = true)
    lateinit var hashCard: String

    @Column(name = "secret_key", nullable = true) // Boleh null karna akan diset di pertama kali register card
    var secretKey: String? = null;

    @Column(name = "pin", nullable = true) // Boleh null karna akan diset di pertama kali register card
    var pin: String? = null;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_address", nullable = true)
    var walletProfile: WalletProfile? = null


}