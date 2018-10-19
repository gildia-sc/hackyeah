package pl.epoint.hackyeah.repository

import pl.epoint.hackyeah.domain.Authority
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data JPA repository for the Authority entity.
 */
@Repository
interface AuthorityRepository : JpaRepository<Authority, String>
