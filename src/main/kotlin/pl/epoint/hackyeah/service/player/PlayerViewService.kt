package pl.epoint.hackyeah.service.player

import pl.epoint.hackyeah.service.view.PlayerView

interface PlayerViewService {

    fun findAll(): List<PlayerView>
}
