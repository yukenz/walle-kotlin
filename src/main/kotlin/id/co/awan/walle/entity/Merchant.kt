package id.co.awan.walle.entity

import jakarta.persistence.*

@Entity
@Table(name = "merchant", schema = "tap2pay")
class Merchant {

    @Id
    @Column(name = "id", nullable = false, length = Integer.MAX_VALUE)
    lateinit var id: String

    @Column(name = "name", nullable = false, length = Integer.MAX_VALUE)
    lateinit var name: String

    @Column(name = "key", nullable = false, length = Integer.MAX_VALUE)
    lateinit var key: String

    // TODO: Make sure this not error
    @OneToMany(mappedBy = "terminal")
    var terminals: Set<Terminal> = emptySet()

    @Column(name = "address", nullable = false, length = Integer.MAX_VALUE)
    lateinit var address: String

}