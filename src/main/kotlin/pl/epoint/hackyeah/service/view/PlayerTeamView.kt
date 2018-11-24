package pl.epoint.hackyeah.service.view

import pl.epoint.hackyeah.domain.Player
import pl.epoint.hackyeah.domain.Team
import java.time.format.DateTimeFormatter

/**
 * @author mslotwinski
 */
open class PlayerTeamView(player: Player, team: Team) {

    private val DATE_PATTERN = "yyyy-MM-dd"

    val teamName: String?

    val playerPosition: String?

    val teammateName: String?

    val teammatePosition: String?

    val createdTime: String?

    val closedTime: String?


    init {
        this.teamName = team.name;
        if (team.attacker == player) {
            this.playerPosition = Position.ATTACKER.toString()
            this.teammatePosition = Position.GOALKEEPER.toString()
            this.teammateName = team.goalkeeper.getDisplayName()
        } else {
            this.playerPosition = Position.GOALKEEPER.toString()
            this.teammatePosition = Position.ATTACKER.toString()
            this.teammateName = team.attacker.getDisplayName()
        }

        this.createdTime = team.createdTime.format(DateTimeFormatter.ofPattern(DATE_PATTERN))
        this.closedTime = team.closedTime?.format(DateTimeFormatter.ofPattern(DATE_PATTERN)) ?: "..."

    }

    enum class Position {
        ATTACKER, GOALKEEPER;

        override fun toString(): String {
            return super.toString().toLowerCase()
        }
    }
}
