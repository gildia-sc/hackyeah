package pl.epoint.hackyeah.web.rest

import pl.epoint.hackyeah.domain.Match
import pl.epoint.hackyeah.domain.Player
import java.time.LocalDateTime

class MatchDto(
    val tableCode: String,
    val alphaAttacker: PlayerDto?,
    val alphaGoalkeeper: PlayerDto?,

    val betaAttacker: PlayerDto?,
    val betaGoalkeeper: PlayerDto?,

    val alphaColor: String,
    val betaColor: String,

    val started: Boolean = false,
    val startTime: LocalDateTime?,
    val endTime: LocalDateTime?,

    val alphaScore: Int,
    val betaScore: Int,

    val reservationStart: LocalDateTime?
) {
    constructor(match: Match) : this(
        match.table.code,
        if (match.playerAlphaAttacker != null) PlayerDto(match.playerAlphaAttacker!!) else null,
        if (match.playerAlphaGoalkeeper != null) PlayerDto(match.playerAlphaGoalkeeper!!) else null,
        if (match.playerBetaAttacker != null) PlayerDto(match.playerBetaAttacker!!) else null,
        if (match.playerBetaGoalkeeper != null) PlayerDto(match.playerBetaGoalkeeper!!) else null,
        match.table.teamAlphaColor,
        match.table.teamBetaColor,
        match.startTime != null,
        match.startTime,
        match.endTime,
        match.teamAlphaScore,
        match.teamBetaScore,
        match.reservationStart)
}

class PlayerDto(
    val id: Long,
    val name: String,
    val image: String?
) {
    constructor(player: Player) : this(player.id!!, "${player.firstName} ${player.lastName}", player.image)
}
