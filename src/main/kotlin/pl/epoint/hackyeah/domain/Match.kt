package pl.epoint.hackyeah.domain

import java.time.LocalDateTime
import javax.persistence.*
import javax.persistence.Table

@Entity
@Table(name = "match")
class Match(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    val id: Long = 0,

    @Column(nullable = false)
    val startTime: LocalDateTime,

    val endTime: LocalDateTime?,

    @OneToOne(optional = false)
    @JoinColumn(name = "foosball_table_id", unique = false, nullable = false, updatable = false)
    val table: FoosballTable,

    @OneToOne
    @JoinColumn(name = "player_a_attacker_id")
    val playerAlphaAttacker: Player?,

    @OneToOne
    @JoinColumn(name = "player_b_attacker_id")
    val playerBetaAttacker: Player?,

    @OneToOne
    @JoinColumn(name = "player_a_goalkeeper_id")
    val playerAlphaGoalkeeper: Player?,

    @OneToOne
    @JoinColumn(name = "player_b_goalkeeper_id")
    val playerBetaGoalkeeper: Player?,

    @OneToOne
    @JoinColumn(name = "team_a_id")
    val teamAlpha: Team?,

    @OneToOne
    @JoinColumn(name = "team_b_id")
    val teamBeta: Team?,

    @Column(name = "player_a_attacker_score")
    val playerAlphaAttackerScore: Int?,

    @Column(name = "player_b_attacker_score")
    val playerBetaAttackerScore: Int?,

    @Column(name = "player_a_goalkeeper_score")
    val playerAlphaGoalkeeperScore: Int?,

    @Column(name = "player_b_goalkeeper_score")
    val playerBetaGoalkeeperScore: Int?,

    @Column(name = "team_a_score")
    val teamAlphaScore: Int = 0,

    @Column(name = "team_b_score")
    val teamBetaScore: Int = 0
)
