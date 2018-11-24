package pl.epoint.hackyeah.service.player

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pl.epoint.hackyeah.repository.MatchRepository
import pl.epoint.hackyeah.repository.PlayerRepository
import pl.epoint.hackyeah.repository.TeamRepository
import pl.epoint.hackyeah.service.view.PlayerTeamView
import pl.epoint.hackyeah.service.view.PlayerView

@Service
@Transactional
class DefaultPlayerViewService(private val playerRepository: PlayerRepository,
                               private val teamRepository: TeamRepository,
                               private val matchRepository: MatchRepository) : PlayerViewService {
    @Transactional(readOnly = true)
    override fun findAll(): List<PlayerView> {
        return playerRepository.findAll().map {
            PlayerView(it, teamRepository.findByPlayer(it),
                matchRepository.findByPlayer(it))
        }
    }

    @Transactional(readOnly = true)
    override fun findTeamsByPlayerId(playerId: Long): List<PlayerTeamView> {
        val player = playerRepository.findById(playerId)
        if (!player.isPresent) {
            return emptyList()
        }
        return teamRepository.findByPlayer(player.get()).map { PlayerTeamView(player.get(), it) }
    }

}
