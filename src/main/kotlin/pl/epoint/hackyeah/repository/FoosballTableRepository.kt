package pl.epoint.hackyeah.repository

import org.springframework.data.jpa.repository.JpaRepository
import pl.epoint.hackyeah.domain.FoosballTable

interface FoosballTableRepository : JpaRepository<FoosballTable, Long> {

    fun findByCode(code: String): FoosballTable?
}
