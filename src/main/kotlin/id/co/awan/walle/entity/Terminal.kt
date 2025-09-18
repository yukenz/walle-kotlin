package id.co.awan.walle.entity

import jakarta.persistence.*

@Entity
@Table(name = "terminal", schema = "walle")
class Terminal {

    @Id
    @Column(name = "id", nullable = false)
    var id: String? = null

    @Column(name = "key")
    var key: String? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id")
    var merchant: Merchant? = null

}