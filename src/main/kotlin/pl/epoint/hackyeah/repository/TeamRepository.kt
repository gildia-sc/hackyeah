package pl.epoint.hackyeah.repository

import org.springframework.data.jpa.repository.JpaRepository
import pl.epoint.hackyeah.domain.Team

interface TeamRepository : JpaRepository<Team, Long>
