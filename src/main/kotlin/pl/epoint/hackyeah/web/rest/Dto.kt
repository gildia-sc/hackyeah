package pl.epoint.hackyeah.web.rest

import pl.epoint.hackyeah.domain.Match
import pl.epoint.hackyeah.domain.Player
import java.time.LocalDateTime

class MatchDto(
    val id: Long,
    val tableCode: String,
    val alphaAttacker: PlayerDto?,
    val alphaGoalkeeper: PlayerDto?,

    val betaAttacker: PlayerDto?,
    val betaGoalkeeper: PlayerDto?,

    val started: Boolean = false,
    val startTime: LocalDateTime?,
    val endTime: LocalDateTime?,

    val alphaScore: Int,
    val betaScore: Int

    
) {
    constructor(match: Match) : this(
        match.id!!,
        match.table.code,
        if (match.playerAlphaAttacker != null) PlayerDto(match.playerAlphaAttacker!!) else null,
        if (match.playerAlphaGoalkeeper != null) PlayerDto(match.playerAlphaGoalkeeper!!) else null,
        if (match.playerBetaAttacker != null) PlayerDto(match.playerBetaAttacker!!) else null,
        if (match.playerBetaGoalkeeper != null) PlayerDto(match.playerBetaGoalkeeper!!) else null,
        match.startTime != null,
        match.startTime,
        match.endTime,
        match.teamAlphaScore,
        match.teamBetaScore)
}

class PlayerDto(
    val id: Long,
    val name: String,
    val image: String?
) {
    constructor(player: Player) : this(player.id!!, "${player.firstName} ${player.lastName}", player.image)
}
