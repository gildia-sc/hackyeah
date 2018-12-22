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
        return matchService.getCurrentMatch(tableCode).let { ResponseEntity.ok(MatchDto(it)) }
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

    @PostMapping("/reset")
    fun resetMatch(@PathVariable tableCode: String): ResponseEntity<MatchDto> {
        return MatchDto(matchService.resetMatch(tableCode))
            .also { publishToWsChannel(tableCode, it) }
            .let { ResponseEntity.ok(it) }
    }

    @PostMapping("/{team}/goal")
    fun scoreGoal(@PathVariable tableCode: String,
                  @PathVariable team: Team,
                  @RequestParam(required = false) position: Position?): ResponseEntity<out Any> {
        return matchService.score(tableCode, team, position)
            ?.let { MatchDto(it) }
            ?.also { matchDto -> publishToWsChannel(tableCode, matchDto) }
            ?.let { matchDto -> ResponseEntity.ok(matchDto) }
            ?: ResponseEntity.badRequest().body("Match does not exist, or is not started.")
    }

    @PostMapping("/{team}/free")
    fun clearPosition(@PathVariable tableCode: String,
                      @PathVariable team: Team,
                      @RequestParam position: Position): ResponseEntity<MatchDto> {
        return matchService.clearPosition(tableCode, team, position)
            ?.let { MatchDto(it) }
            ?.also { matchDto -> publishToWsChannel(tableCode, matchDto) }
            ?.let { matchDto -> ResponseEntity.ok(matchDto) }
            ?: ResponseEntity.notFound().build()
    }

    @PostMapping("/{team}/switch")
    fun switchPositions(@PathVariable tableCode: String,
                        @PathVariable team: Team): ResponseEntity<MatchDto> {
        return matchService.switchPositions(tableCode, team)
            ?.let { MatchDto(it) }
            ?.also { matchDto -> publishToWsChannel(tableCode, matchDto) }
            ?.let { matchDto -> ResponseEntity.ok(matchDto) }
            ?: ResponseEntity.notFound().build()
    }

    private fun publishToWsChannel(channel: String, match: MatchDto) {
        simpMessagingTemplate.convertAndSend(channel, match)
    }
}
