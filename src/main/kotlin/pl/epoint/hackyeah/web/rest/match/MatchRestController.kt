package pl.epoint.hackyeah.web.rest.match

import org.springframework.http.ResponseEntity
import org.springframework.messaging.simp.SimpMessagingTemplate
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
class MatchRestController(private val matchService: MatchService,
                          private val playerRepository: PlayerRepository,
                          private val simpMessagingTemplate: SimpMessagingTemplate) {

    @GetMapping
    fun getMatchForTable(@PathVariable tableCode: String): ResponseEntity<MatchDto> {
        return matchService.getCurrentMatch(tableCode)
            ?.let { ResponseEntity.ok(MatchDto(it)) }
            ?: ResponseEntity.notFound().build()
    }

    @PostMapping("/{team}")
    fun takePosition(@PathVariable tableCode: String,
                     @PathVariable team: Team,
                     @RequestParam position: Position,
                     principal: Principal): ResponseEntity<MatchDto> {
        val player = playerRepository.findOneByLogin(principal.name).orElseThrow {
            RuntimeException()
        }
        val match = MatchDto(matchService.takePosition(tableCode, player, team, position))
        publishToWsChannel(tableCode, match)
        return ResponseEntity.ok(match)
    }

    @PostMapping("/{team}/goal")
    fun scoreGoal(@PathVariable tableCode: String,
                  @PathVariable team: Team,
                  @RequestParam(required = false) position: Position?): ResponseEntity<MatchDto> {
        val match = MatchDto(matchService.score(tableCode, team, position))
        publishToWsChannel(tableCode, match)
        return ResponseEntity.ok(match)
    }

    @PostMapping("/{team}/free")
    fun clearPosition(@PathVariable tableCode: String,
                      @PathVariable team: Team,
                      @RequestParam position: Position): ResponseEntity<MatchDto> {
        val match = MatchDto(matchService.clearPosition(tableCode, team, position))
        publishToWsChannel(tableCode, match)
        return ResponseEntity.ok(match)
    }

    @PostMapping("/{team}/switch")
    fun switchPositions(@PathVariable tableCode: String,
                        @PathVariable team: Team): ResponseEntity<MatchDto> {
        val match = MatchDto(matchService.switchPositions(tableCode, team))
        publishToWsChannel(tableCode, match)
        return ResponseEntity.ok(match)
    }

    private fun publishToWsChannel(channel: String, match: MatchDto) {
        simpMessagingTemplate.convertAndSend(channel, match)
    }
}
