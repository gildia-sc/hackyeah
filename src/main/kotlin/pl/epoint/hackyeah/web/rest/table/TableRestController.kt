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

    @GetMapping
    fun getAll(): ResponseEntity<List<FoosballTableDto>> {
        return ResponseEntity.ok(foosballTableService.findAll())
    }

    @GetMapping(value = "/{code}")
    fun getByCode(@PathVariable("code") code: String): ResponseEntity<FoosballTableDto> {
        return foosballTableService.findByCode(FoosballTableCode(code))
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
    }

    @PostMapping
    fun insertTable(@RequestBody tableDto: FoosballTableDto): ResponseEntity<FoosballTableDto> {
        val newTableDto = foosballTableService.addTable(FoosballTableCode(tableDto.code),
            TeamColor(tableDto.teamAlphaColor),
            TeamColor(tableDto.teamBetaColor))
        return ResponseEntity
            .created(URI("/tables/${newTableDto.code}"))
            .body(newTableDto)
    }

    @PutMapping(value = "/{code}")
    fun updateTable(@PathVariable("code") code: String,
                    @RequestBody tableDto: FoosballTableDto): ResponseEntity<FoosballTableDto> {

        foosballTableService.findByCode(FoosballTableCode(code)) ?: return ResponseEntity.notFound().build()

        val updatedTableDto = foosballTableService.updateTable(FoosballTableCode(tableDto.code),
            TeamColor(tableDto.teamAlphaColor),
            TeamColor(tableDto.teamBetaColor))


        return ResponseEntity
            .created(URI("/tables/${updatedTableDto.code}"))
            .body(updatedTableDto)
    }

    @DeleteMapping(value = "/{code}")
    fun deleteTable(@PathVariable("code") code: String) {
        foosballTableService.deleteByCode(FoosballTableCode(code))
    }
}
