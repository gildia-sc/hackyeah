package pl.epoint.hackyeah.service.view

import pl.epoint.hackyeah.domain.Match
import pl.epoint.hackyeah.domain.Player
import pl.epoint.hackyeah.domain.Team
import java.util.stream.Collectors

/**
 * @author mslotwinski
 */
open class PlayerView(player: Player, teams: List<Team>, matches: List<Match>) {

    val id: Long?

    val displayName: String?

    val image: String?

    val teams: String?

    val numberOfWins: Int?

    val numberOfLoses: Int?

    init {
        this.id = player.id
        this.displayName = player.getDisplayName()
        this.image = player.image
        this.teams = teams.stream().map { it.name }.collect(Collectors.joining(", "))
        this.numberOfWins = matches.filter {
            (it.teamAlphaScore > it.teamBetaScore)
                && (it.playerAlphaAttacker == player || it.playerAlphaGoalkeeper == player)
            || (it.teamBetaScore > it.teamAlphaScore)
                && (it.playerBetaAttacker == player || it.playerBetaGoalkeeper == player) }.size
        this.numberOfLoses = matches.size - this.numberOfWins
    }
}
