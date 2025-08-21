package id.co.awan.walle.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "hsm", schema = "tap2pay")
class Hsm {

    @Id
    @Column(name = "id", nullable = true)
    var id: String? = null

    @Column(name = "owner_address", nullable = true) // Boleh null karna akan diset di pertama kali register card
    var ownerAddress: String? = null;

    @Column(name = "secret_key", nullable = true) // Boleh null karna akan diset di pertama kali register card
    var secretKey: String? = null;

    @Column(name = "pin", nullable = true) // Boleh null karna akan diset di pertama kali register card
    var pin: String? = null;

}