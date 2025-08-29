package id.co.awan.walle.entity

import jakarta.persistence.*

@Entity
@Table(name = "wallet_profile", schema = "tap2pay")
class WalletProfile {

    @Id
//    @Generated(event = [EventType.INSERT])
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wallet_address", nullable = false)
    lateinit var walletAddress: String

    @Column(name = "username", nullable = true, unique = true)
    lateinit var username: String

    @Column(name = "email", nullable = true, unique = true)
    lateinit var email: String

    @Column(name = "photo_profile", nullable = true)
    var photo_profile: String? = null

    @Column(name = "username_telegram", nullable = true, unique = true)
    var usernameTelegram: String? = null

    @OneToMany(mappedBy = "walletProfile")
    var hsm: Set<Hsm> = emptySet()

}