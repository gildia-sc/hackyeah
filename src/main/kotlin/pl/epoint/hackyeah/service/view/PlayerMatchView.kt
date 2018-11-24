package pl.epoint.hackyeah.service.view

import pl.epoint.hackyeah.domain.Match
import java.time.format.DateTimeFormatter

/**
 * @author mslotwinski
 */
open class PlayerMatchView(match: Match) {

    private val DATE_TIME_PATTERN = "yyyy-MM-dd h:mm"

    val startTime: String?

    var teamAlphaName: String?

    var teamBetaName: String?

    val teamAlphaScore: Int?

    val teamBetaScore: Int?

    val attackerAlphaImage: String?

    val goalkeeperAlphaImage: String?

    val attackerBetaImage: String?

    val goalkeeperBetaImage: String?

    init {
        this.startTime = match.startTime?.format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)) ?: "-"
        this.teamAlphaName = match.teamAlpha?.name ?: "-"
        this.teamBetaName = match.teamBeta?.name ?: "-"
        this.teamAlphaScore = match.teamAlphaScore
        this.teamBetaScore = match.teamBetaScore
        this.attackerAlphaImage = match.playerAlphaAttacker?.image
        this.goalkeeperAlphaImage = match.playerAlphaGoalkeeper?.image
        this.attackerBetaImage = match.playerBetaAttacker?.image
        this.goalkeeperBetaImage = match.playerBetaGoalkeeper?.image
    }
}
