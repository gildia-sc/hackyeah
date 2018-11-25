package pl.epoint.hackyeah.service.player

import pl.epoint.hackyeah.service.dto.UserDTO
import pl.epoint.hackyeah.service.view.PlayerMatchView
import pl.epoint.hackyeah.service.view.PlayerTeamView
import pl.epoint.hackyeah.service.view.PlayerView

interface PlayerViewService {

    fun getPlayerByPlayerId(id: Long): UserDTO?

    fun updatePlayer(userDTO: UserDTO)

    fun findAll(): List<PlayerView>

    fun findTeamsByPlayerId(playerId: Long): List<PlayerTeamView>

    fun findMatchesByPlayerId(playerId: Long): List<PlayerMatchView>

    fun deleteByPlayerId(id: Long)

    fun validateLoginTaken(id: Long, login: String): Boolean

    fun validateEmailTaken(id: Long, email: String): Boolean
}
