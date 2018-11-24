package pl.epoint.hackyeah.web.rest

class MatchDto(
    val id: Long,
    val tableCode: String,
    val alphaAttacker: PlayerDto?,
    val alphaGoalkeeper: PlayerDto?,

    val betaAttacker: PlayerDto?,
    val betaGoalkeeper: PlayerDto?,

    val alphaScore: Int,
    val betaScore: Int

)

class PlayerDto(
    val id: Long,
    val name: String
)
