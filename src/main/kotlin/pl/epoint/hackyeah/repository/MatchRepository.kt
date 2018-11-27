package pl.epoint.hackyeah.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import pl.epoint.hackyeah.domain.Match
import pl.epoint.hackyeah.domain.Player

interface MatchRepository : JpaRepository<Match, Long> {

    fun findByTableCodeAndStartTimeNotNullAndEndTimeNull(tableCode: String): Match?

    fun findByTableCodeAndEndTimeNull(tableCode: String): Match?

    @Query("SELECT m FROM Match m WHERE m.playerAlphaAttacker = :player OR m.playerBetaAttacker = :player " +
        "OR m.playerAlphaGoalkeeper = :player OR m.playerBetaGoalkeeper = :player")
    fun findByPlayer(@Param("player") player: Player): List<Match>
}
