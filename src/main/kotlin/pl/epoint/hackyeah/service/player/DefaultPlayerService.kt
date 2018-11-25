package pl.epoint.hackyeah.service.player

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pl.epoint.hackyeah.domain.Player
import pl.epoint.hackyeah.repository.MatchRepository
import pl.epoint.hackyeah.repository.PlayerRepository
import pl.epoint.hackyeah.repository.TeamRepository
import pl.epoint.hackyeah.service.dto.UserDTO
import pl.epoint.hackyeah.service.view.PlayerMatchView
import pl.epoint.hackyeah.service.view.PlayerTeamView
import pl.epoint.hackyeah.service.view.PlayerView

@Service
@Transactional
class DefaultPlayerService(private val playerRepository: PlayerRepository,
                           private val teamRepository: TeamRepository,
                           private val matchRepository: MatchRepository) : PlayerViewService {

    override fun getPlayerByPlayerId(id: Long): UserDTO {
        val player = playerRepository.findById(id)
        if (player.isPresent) {
            return UserDTO(player.get())
        }
        return UserDTO(Player())
    }

    override fun updatePlayer(userDTO: UserDTO) {
        val playerId: Long = userDTO.id ?: return
        val player = playerRepository.findById(playerId)

        if (!player.isPresent) {
            return
        }

        playerRepository.save(player.get().update(userDTO))
    }

    @Transactional(readOnly = true)
    override fun findAll(): List<PlayerView> {
        return playerRepository.findAll()
            .filter { it.activated }
            .map { PlayerView(it, teamRepository.findByPlayer(it), matchRepository.findByPlayer(it)) }
    }

    @Transactional(readOnly = true)
    override fun findTeamsByPlayerId(playerId: Long): List<PlayerTeamView> {
        val player = playerRepository.findById(playerId)
        if (!player.isPresent) {
            return emptyList()
        }
        return teamRepository.findByPlayer(player.get()).map { PlayerTeamView(player.get(), it) }
    }

    @Transactional(readOnly = true)
    override fun findMatchesByPlayerId(playerId: Long): List<PlayerMatchView> {
        val player = playerRepository.findById(playerId)
        if (!player.isPresent) {
            return emptyList()
        }
        return matchRepository.findByPlayer(player.get()).map { PlayerMatchView(it) }
    }

    override fun deleteByPlayerId(id: Long) {
        val player = playerRepository.findById(id)
        if (player.isPresent) {
            player.get().activated = false
            playerRepository.save(player.get())
        }
    }

    override fun validateLoginTaken(id: Long, login: String): Boolean {
        return playerRepository.findOneByLogin(login.toLowerCase()).map { it.id != id }.orElse(false)
    }

    override fun validateEmailTaken(id: Long, email: String): Boolean {
        return playerRepository.findOneByEmailIgnoreCase(email).map { it.id != id }.orElse(false)
    }

}
