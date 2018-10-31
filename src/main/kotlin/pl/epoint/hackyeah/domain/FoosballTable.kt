package pl.epoint.hackyeah.domain

import javax.persistence.*
import javax.persistence.Table

@Entity
@Table(name = "foosball_table")
class FoosballTable(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    val id: Long = 0,

    @Column(nullable = false)
    val code: String,

    @Column(name = "team_a_color", nullable = false)
    val teamAlphacolor: String,

    @Column(name = "team_b_color", nullable = false)
    val teamBetacolor: String
)
