package pl.epoint.hackyeah.web.rest.table

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pl.epoint.hackyeah.service.FoosballTableCode
import pl.epoint.hackyeah.service.TeamColor
import pl.epoint.hackyeah.service.dto.FoosballTableDto
import pl.epoint.hackyeah.service.table.FoosballTableService
import java.net.URI

@RestController
@RequestMapping("/api/tables")
class TableRestController(private val foosballTableService: FoosballTableService) {

    @PostMapping
    fun addTable(@RequestBody tableDto: FoosballTableDto): ResponseEntity<FoosballTableDto> {
        val newTableDto = foosballTableService.addTable(FoosballTableCode(tableDto.code),
            TeamColor(tableDto.teamAlphaColor),
            TeamColor(tableDto.teamBetaColor))
        return ResponseEntity
            .created(URI("/tables/${newTableDto.code}"))
            .body(newTableDto)
    }

    @GetMapping
    fun getAll(): ResponseEntity<List<FoosballTableDto>> {
        return ResponseEntity.ok(foosballTableService.findAll())
    }
}
