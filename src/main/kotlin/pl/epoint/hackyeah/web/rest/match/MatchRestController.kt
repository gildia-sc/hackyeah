package pl.epoint.hackyeah.web.rest.match

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pl.epoint.hackyeah.repository.PlayerRepository
import pl.epoint.hackyeah.service.match.MatchService
import pl.epoint.hackyeah.service.match.Position
import pl.epoint.hackyeah.service.match.Team
import pl.epoint.hackyeah.web.rest.MatchDto
import java.lang.RuntimeException
import java.security.Principal

@RestController
@RequestMapping("/api/matches/{tableCode}")
class MatchRestController(val matchService: MatchService,
                          val playerRepository: PlayerRepository) {

    @GetMapping
    fun getMatchForTable(@PathVariable tableCode: String): ResponseEntity<MatchDto> {
        val match = matchService.getMatch(tableCode)
        return if (match != null) return ResponseEntity.ok(MatchDto(match))
            else ResponseEntity.notFound().build<MatchDto>()
    }

    @PostMapping("/{team}")
    fun takePosition(@PathVariable tableCode: String,
                     @PathVariable team: Team,
                     @RequestParam position: Position,
                     principal: Principal): ResponseEntity<MatchDto> {
        val player = playerRepository.findOneByLogin(principal.name).orElseThrow {
            RuntimeException()
        }
        val match = matchService.takePosition(tableCode, player, team, position)
        return ResponseEntity.ok(MatchDto(match))
    }

    @PostMapping("/{team}/goal")
    fun scoreGoal(@PathVariable tableCode: String,
                  @PathVariable team: Team,
                  @RequestParam(required = false) position: Position?): ResponseEntity<MatchDto> {
        val match = matchService.score(tableCode, team, position)
        return ResponseEntity.ok(MatchDto(match))
    }

    @PostMapping("/{team}/free")
    fun clearPosition(@PathVariable tableCode: String,
                      @PathVariable team: Team,
                      @RequestParam position: Position): ResponseEntity<MatchDto> {
        val match = matchService.clearPosition(tableCode, team, position)
        return ResponseEntity.ok(MatchDto(match))
    }

    @PostMapping("/{team}/switch")
    fun switchPositions(@PathVariable tableCode: String,
                        @PathVariable team: Team): ResponseEntity<MatchDto> {
        val match = matchService.switchPositions(tableCode, team)
        return ResponseEntity.ok(MatchDto(match))
    }
}
