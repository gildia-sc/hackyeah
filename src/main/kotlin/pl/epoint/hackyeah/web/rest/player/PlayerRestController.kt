package pl.epoint.hackyeah.web.rest.player

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pl.epoint.hackyeah.service.player.DefaultPlayerService
import pl.epoint.hackyeah.service.view.PlayerMatchView
import pl.epoint.hackyeah.service.view.PlayerTeamView
import pl.epoint.hackyeah.service.view.PlayerView

/**
 * @author mslotwinski
 */
@RestController
@RequestMapping("/api/players")
class PlayerRestController(private val playerService: DefaultPlayerService) {

    @GetMapping("/view")
    fun getAll(): ResponseEntity<List<PlayerView>> {
        return ResponseEntity.ok(playerService.findAll())
    }

    @GetMapping("/view/{id}/teams")
    fun getPlayerTeams(@PathVariable id: Long): ResponseEntity<List<PlayerTeamView>> {
        return ResponseEntity.ok(playerService.findTeamsByPlayerId(id))
    }

    @GetMapping("/view/{id}/matches")
    fun getPlayerMatches(@PathVariable id: Long): ResponseEntity<List<PlayerMatchView>> {
        return ResponseEntity.ok(playerService.findMatchesByPlayerId(id))
    }

    @DeleteMapping("/{id}")
    fun deletePlayer(@PathVariable id: Long){
        playerService.deleteByPlayerId(id)
    }
}
