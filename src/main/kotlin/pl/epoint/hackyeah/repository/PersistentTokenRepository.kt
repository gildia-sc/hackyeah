package pl.epoint.hackyeah.repository

import pl.epoint.hackyeah.domain.PersistentToken
import pl.epoint.hackyeah.domain.Player
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

import java.time.LocalDate

/**
 * Spring Data JPA repository for the PersistentToken entity.
 */
@Repository
interface PersistentTokenRepository : JpaRepository<PersistentToken, String> {

    fun findByPlayer(player: Player): List<PersistentToken>

    fun findByTokenDateBefore(localDate: LocalDate): List<PersistentToken>

}
