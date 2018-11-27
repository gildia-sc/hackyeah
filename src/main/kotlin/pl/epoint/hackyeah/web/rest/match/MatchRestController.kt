package pl.epoint.hackyeah.web.rest.match

import org.springframework.http.ResponseEntity
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.web.bind.annotation.*
import pl.epoint.hackyeah.extension.unwrapOrThrow
import pl.epoint.hackyeah.repository.PlayerRepository
import pl.epoint.hackyeah.service.match.MatchService
import pl.epoint.hackyeah.service.match.Position
import pl.epoint.hackyeah.service.match.Team
import pl.epoint.hackyeah.web.rest.MatchDto
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
        return playerRepository.findOneByLogin(principal.name)
            .unwrapOrThrow { IllegalStateException("Player ${principal.name} not found") }
            .let { matchService.takePosition(tableCode, it, team, position) }
            .let { match -> MatchDto(match) }
            .also { matchDto -> publishToWsChannel(tableCode, matchDto) }
            .let { matchDto -> ResponseEntity.ok(matchDto) }
    }

    @PostMapping("/{team}/goal")
    fun scoreGoal(@PathVariable tableCode: String,
                  @PathVariable team: Team,
                  @RequestParam(required = false) position: Position?): ResponseEntity<MatchDto> {
        return MatchDto(matchService.score(tableCode, team, position))
            .also { matchDto -> publishToWsChannel(tableCode, matchDto) }
            .let { matchDto -> ResponseEntity.ok(matchDto) }
    }

    @PostMapping("/{team}/free")
    fun clearPosition(@PathVariable tableCode: String,
                      @PathVariable team: Team,
                      @RequestParam position: Position): ResponseEntity<MatchDto> {
        return MatchDto(matchService.clearPosition(tableCode, team, position))
            .also { matchDto -> publishToWsChannel(tableCode, matchDto) }
            .let { matchDto -> ResponseEntity.ok(matchDto) }
    }

    @PostMapping("/{team}/switch")
    fun switchPositions(@PathVariable tableCode: String,
                        @PathVariable team: Team): ResponseEntity<MatchDto> {
        return MatchDto(matchService.switchPositions(tableCode, team))
            .also { matchDto -> publishToWsChannel(tableCode, matchDto) }
            .let { matchDto -> ResponseEntity.ok(matchDto) }
    }

    private fun publishToWsChannel(channel: String, match: MatchDto) {
        simpMessagingTemplate.convertAndSend(channel, match)
    }
}
