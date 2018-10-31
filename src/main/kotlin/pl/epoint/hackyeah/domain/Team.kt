package pl.epoint.hackyeah.domain

import java.io.Serializable
import javax.persistence.*
import javax.persistence.Table

@Entity
@Table(name = "team")
class Team(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    val id: Long = 0,

    @Column(nullable = false)
    var name: String,

    @OneToOne(optional = false)
    val attacker: Player,

    @OneToOne(optional = false)
    val goalkeeper: Player
): Serializable {

    companion object {
        private val serialVersionUID = 1L
    }
}
