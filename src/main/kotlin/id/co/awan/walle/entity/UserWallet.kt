package id.co.awan.walle.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "user_wallet", schema = "walle")
class UserWallet {

    @Id
    @Column(name = "id", nullable = false)
    var address: String? = null

}