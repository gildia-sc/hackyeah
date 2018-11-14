package pl.epoint.hackyeah.repository

import pl.epoint.hackyeah.domain.Player
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

import java.time.Instant
import java.util.Optional

/**
 * Spring Data JPA repository for the User entity.
 */
@Repository
interface PlayerRepository : JpaRepository<Player, Long> {

    fun findOneByActivationKey(activationKey: String): Optional<Player>

    fun findAllByActivatedIsFalseAndCreatedDateBefore(dateTime: Instant): List<Player>

    fun findOneByResetKey(resetKey: String): Optional<Player>

    fun findOneByEmailIgnoreCase(email: String): Optional<Player>

    fun findOneByLogin(login: String): Optional<Player>

    @EntityGraph(attributePaths = arrayOf("authorities"))
    fun findOneWithAuthoritiesById(id: Long?): Optional<Player>

    @EntityGraph(attributePaths = arrayOf("authorities"))
    fun findOneWithAuthoritiesByLogin(login: String): Optional<Player>

    @EntityGraph(attributePaths = arrayOf("authorities"))
    fun findOneWithAuthoritiesByEmail(email: String): Optional<Player>

    fun findAllByLoginNot(pageable: Pageable, login: String): Page<Player>

    fun findAllByLogin(login: String): List<Player>
}
