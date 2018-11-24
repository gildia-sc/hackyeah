package pl.epoint.hackyeah.web.rest.player

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pl.epoint.hackyeah.service.player.DefaultPlayerViewService
import pl.epoint.hackyeah.service.view.PlayerView

/**
 * @author mslotwinski
 */
@RestController
@RequestMapping("/api/players/view")
class PlayerViewRestController(private val playerViewService: DefaultPlayerViewService) {

    @GetMapping
    fun getAll(): ResponseEntity<List<PlayerView>> {
        return ResponseEntity.ok(playerViewService.findAll())
    }
}
