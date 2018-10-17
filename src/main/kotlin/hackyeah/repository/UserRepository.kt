package hackyeah.repository

import hackyeah.domain.User
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
interface UserRepository : JpaRepository<User, Long> {

    fun findOneByActivationKey(activationKey: String): Optional<User>

    fun findAllByActivatedIsFalseAndCreatedDateBefore(dateTime: Instant): List<User>

    fun findOneByResetKey(resetKey: String): Optional<User>

    fun findOneByEmailIgnoreCase(email: String): Optional<User>

    fun findOneByLogin(login: String): Optional<User>

    @EntityGraph(attributePaths = arrayOf("authorities"))
    fun findOneWithAuthoritiesById(id: Long?): Optional<User>

    @EntityGraph(attributePaths = arrayOf("authorities"))
    fun findOneWithAuthoritiesByLogin(login: String): Optional<User>

    @EntityGraph(attributePaths = arrayOf("authorities"))
    fun findOneWithAuthoritiesByEmail(email: String): Optional<User>

    fun findAllByLoginNot(pageable: Pageable, login: String): Page<User>
}
