package pl.epoint.hackyeah.service.table

import pl.epoint.hackyeah.service.FoosballTableCode
import pl.epoint.hackyeah.service.TeamColor
import pl.epoint.hackyeah.service.dto.FoosballTableDto

interface FoosballTableService {

    fun addTable(tableCode: FoosballTableCode, teamAlphaColor: TeamColor, teamBetaColor: TeamColor): FoosballTableDto

    fun updateTable(tableCode: FoosballTableCode, teamAlphaColor: TeamColor, teamBetaColor: TeamColor): FoosballTableDto

    fun findAll(): List<FoosballTableDto>

    fun findByCode(code: FoosballTableCode): FoosballTableDto?

    fun deleteByCode(foosballTableCode: FoosballTableCode)
}
