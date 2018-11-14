package pl.epoint.hackyeah.service.table

import pl.epoint.hackyeah.service.FoosballTableCode
import pl.epoint.hackyeah.service.TeamColor
import pl.epoint.hackyeah.service.dto.FoosballTableDto

interface FoosballTableService {

    fun addTable(code: FoosballTableCode, teamAlphaColor: TeamColor, teamBetaColor: TeamColor): FoosballTableDto

    fun updateTeamAlphaColor(tableCode: FoosballTableCode, newColor: TeamColor): FoosballTableDto

    fun updateTeamBetaColor(tableCode: FoosballTableCode, newColor: TeamColor): FoosballTableDto

    fun findAll(): List<FoosballTableDto>
}
