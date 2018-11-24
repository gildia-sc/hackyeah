package pl.epoint.hackyeah.domain

import pl.epoint.hackyeah.service.TeamColor
import pl.epoint.hackyeah.service.dto.FoosballTableDto
import javax.persistence.*
import javax.persistence.Table
import javax.validation.constraints.NotNull

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
    val teamAlphaColor: String,

    @Column(name = "team_b_color", nullable = false)
    val teamBetaColor: String,

    @NotNull
    @Column(nullable = false)
    var activated: Boolean = true
) {

    fun toDto(): FoosballTableDto {
        return FoosballTableDto(code, teamAlphaColor, teamBetaColor)
    }

    fun updateTeamAlphaColor(color: TeamColor): FoosballTable {
        return FoosballTable(id, code, color.raw, teamBetaColor)
    }

    fun updateTeamBetaColor(color: TeamColor): FoosballTable {
        return FoosballTable(id, code, teamAlphaColor, color.raw)
    }
}
