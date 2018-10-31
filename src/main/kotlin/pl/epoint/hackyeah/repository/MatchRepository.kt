package pl.epoint.hackyeah.repository

import org.springframework.data.jpa.repository.JpaRepository
import pl.epoint.hackyeah.domain.Match

interface MatchRepository : JpaRepository<Match, Long>
