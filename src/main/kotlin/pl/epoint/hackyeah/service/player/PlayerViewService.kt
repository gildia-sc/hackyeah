package pl.epoint.hackyeah.service.player

import pl.epoint.hackyeah.service.view.PlayerMatchView
import pl.epoint.hackyeah.service.view.PlayerTeamView
import pl.epoint.hackyeah.service.view.PlayerView

interface PlayerViewService {

    fun findAll(): List<PlayerView>

    fun findTeamsByPlayerId(playerId: Long): List<PlayerTeamView>

    fun findMatchesByPlayerId(playerId: Long): List<PlayerMatchView>
}
