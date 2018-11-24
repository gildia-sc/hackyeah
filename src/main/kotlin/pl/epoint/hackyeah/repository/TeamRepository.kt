package pl.epoint.hackyeah.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import pl.epoint.hackyeah.domain.Player
import pl.epoint.hackyeah.domain.Team

interface TeamRepository : JpaRepository<Team, Long> {

    @Query("SELECT t FROM Team t WHERE t.attacker = :player OR t.goalkeeper = :player")
    fun findByPlayer(@Param("player") player: Player): List<Team>
}
