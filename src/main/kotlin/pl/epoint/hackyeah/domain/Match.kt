package pl.epoint.hackyeah.domain

import java.time.LocalDateTime
import javax.persistence.*
import javax.persistence.Table

@Entity
@Table(name = "match")
class Match(
    @ManyToOne(optional = false)
    @JoinColumn(name = "foosball_table_id", unique = false, nullable = false, updatable = false)
    val table: FoosballTable,

    var reservationStart: LocalDateTime? = null
) {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    var id: Long? = null

    var startTime: LocalDateTime? = null

    var endTime: LocalDateTime? = null

    @ManyToOne
    @JoinColumn(name = "player_a_attacker_id")
    var playerAlphaAttacker: Player? = null

    @ManyToOne
    @JoinColumn(name = "player_b_attacker_id")
    var playerBetaAttacker: Player? = null

    @ManyToOne
    @JoinColumn(name = "player_a_goalkeeper_id")
    var playerAlphaGoalkeeper: Player? = null

    @ManyToOne
    @JoinColumn(name = "player_b_goalkeeper_id")
    var playerBetaGoalkeeper: Player? = null

    @ManyToOne
    @JoinColumn(name = "team_a_id")
    var teamAlpha: Team? = null

    @ManyToOne
    @JoinColumn(name = "team_b_id")
    var teamBeta: Team? = null

    @Column(name = "player_a_attacker_score")
    var playerAlphaAttackerScore: Int = 0

    @Column(name = "player_b_attacker_score")
    var playerBetaAttackerScore: Int = 0

    @Column(name = "player_a_goalkeeper_score")
    var playerAlphaGoalkeeperScore: Int = 0

    @Column(name = "player_b_goalkeeper_score")
    var playerBetaGoalkeeperScore: Int = 0

    @Column(name = "team_a_score")
    var teamAlphaScore: Int = 0

    @Column(name = "team_b_score")
    var teamBetaScore: Int = 0
}
