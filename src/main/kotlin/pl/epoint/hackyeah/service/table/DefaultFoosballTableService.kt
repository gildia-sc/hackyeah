package pl.epoint.hackyeah.service.table

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pl.epoint.hackyeah.domain.FoosballTable
import pl.epoint.hackyeah.repository.FoosballTableRepository
import pl.epoint.hackyeah.service.FoosballTableCode
import pl.epoint.hackyeah.service.TeamColor
import pl.epoint.hackyeah.service.dto.FoosballTableDto

@Service
@Transactional
class DefaultFoosballTableService(private val foosballTableRepository: FoosballTableRepository) : FoosballTableService {

    override fun addTable(code: FoosballTableCode,
                          teamAlphaColor: TeamColor,
                          teamBetaColor: TeamColor): FoosballTableDto {
        val newTable = FoosballTable(0, code.raw, teamAlphaColor.raw, teamBetaColor.raw)
        return foosballTableRepository.save(newTable).toDto()
    }

    @Transactional(readOnly = true)
    override fun findAll(): List<FoosballTableDto> {
        return foosballTableRepository.findAll().map { it.toDto() }
    }

    override fun updateTeamAlphaColor(tableCode: FoosballTableCode, newColor: TeamColor): FoosballTableDto {
        return foosballTableRepository.findByCode(tableCode.raw)
            ?.let { foosballTableRepository.save(it.updateTeamAlphaColor(newColor)) }
            ?.let { it.toDto() }
            ?: throw IllegalStateException("Table ${tableCode.raw} does not exist")
    }

    override fun updateTeamBetaColor(tableCode: FoosballTableCode, newColor: TeamColor): FoosballTableDto {
        return foosballTableRepository.findByCode(tableCode.raw)
            ?.let { foosballTableRepository.save(it.updateTeamBetaColor(newColor)) }
            ?.let { it.toDto() }
            ?: throw IllegalStateException("Table ${tableCode.raw} does not exist")
    }

}
