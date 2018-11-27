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

    @GetMapping("/{code}")
    fun getByCode(@PathVariable("code") code: String): ResponseEntity<FoosballTableDto> {
        return foosballTableService.findByCode(FoosballTableCode(code))
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
    }

    @PostMapping("/login-taken")
    fun loginTaken(@RequestBody code: String): Boolean {
        return foosballTableService.findByCode(FoosballTableCode(code)) != null
    }

    @PostMapping
    fun insertTable(@RequestBody tableDto: FoosballTableDto): ResponseEntity<FoosballTableDto> {
        return foosballTableService.addTable(FoosballTableCode(tableDto.code),
            TeamColor(tableDto.teamAlphaColor),
            TeamColor(tableDto.teamBetaColor))
            .let { table -> ResponseEntity.created(URI("/tables/${table.code}")).body(table) }
    }

    @PutMapping("/{code}")
    fun updateTable(@PathVariable("code") code: String,
                    @RequestBody tableDto: FoosballTableDto): ResponseEntity<FoosballTableDto> {
        return foosballTableService.findByCode(FoosballTableCode(code))
            ?.let {
                foosballTableService.updateTable(FoosballTableCode(tableDto.code),
                    TeamColor(tableDto.teamAlphaColor),
                    TeamColor(tableDto.teamBetaColor))
            }
            ?.let { updatedTable -> ResponseEntity.created(URI("/tables/${updatedTable.code}")).body(updatedTable) }
            ?: ResponseEntity.notFound().build()
    }

    @DeleteMapping("/{code}")
    fun deleteTable(@PathVariable("code") code: String) {
        foosballTableService.deleteByCode(FoosballTableCode(code))
    }
}
