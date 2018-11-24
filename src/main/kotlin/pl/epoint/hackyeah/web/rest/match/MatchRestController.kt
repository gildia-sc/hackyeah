package pl.epoint.hackyeah.web.rest.match

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pl.epoint.hackyeah.web.rest.MatchDto
import java.security.Principal

@RestController
@RequestMapping("/api/matches/{tableCode}")
class MatchRestController {

    @GetMapping
    fun getMatchForTable(@PathVariable tableCode: String): ResponseEntity<MatchDto?> {
        return ResponseEntity.ok().build()
    }

    @PostMapping("/{team}")
    fun takePosition(@PathVariable tableCode: String,
                     @PathVariable team: String,
                     @RequestParam position: String,
                     currentPlayer: Principal): ResponseEntity<MatchDto> {
        return ResponseEntity
            .ok(MatchDto(1L, tableCode, null, null, null, null, 1, 2))
    }

    @PostMapping("/{team}/goal")
    fun scoreGoal(@PathVariable tableCode: String,
                  @PathVariable team: String,
                  @RequestParam(required = false) position: String?): ResponseEntity<MatchDto> {
        return ResponseEntity
            .ok(MatchDto(1L, tableCode, null, null, null, null, 2, 3))
    }

    @PostMapping("/{team}/free")
    fun clearPosition(@PathVariable tableCode: String,
                      @PathVariable team: String,
                      @RequestParam position: String): ResponseEntity<MatchDto> {
        TODO()
    }

    @PostMapping("/{team}/switch")
    fun switchPositions(@PathVariable tableCode: String,
                        @PathVariable team: String): ResponseEntity<MatchDto> {
        TODO()
    }

}
