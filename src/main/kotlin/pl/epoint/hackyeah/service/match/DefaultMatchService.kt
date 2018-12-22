package pl.epoint.hackyeah.service.match

import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pl.epoint.hackyeah.domain.Match
import pl.epoint.hackyeah.domain.Player
import pl.epoint.hackyeah.repository.FoosballTableRepository
import pl.epoint.hackyeah.repository.MatchRepository
import java.time.LocalDateTime

/**
 * @author Piotr Wolny
 */
@Service
@Transactional
class DefaultMatchService(private val tableRepository: FoosballTableRepository,
                          private val matchRepository: MatchRepository) : MatchService {

    override fun getCurrentMatch(tableCode: String): Match {
        return tableRepository.findByCode(tableCode)
            .let { matchRepository.findByTableCodeAndEndTimeNull(tableCode) ?: Match(it) }
    }

    @CacheEvict("match-reservations", allEntries = true)
    override fun resetMatch(tableCode: String): Match {
        val match = getCurrentMatch(tableCode)
        if (match.startTime != null) {
            match.endTime = LocalDateTime.now()
            return tableRepository.findByCode(tableCode).let { matchRepository.save(Match(it)) }
        }
        return match
    }

    @CacheEvict("match-reservations", allEntries = true)
    override fun takePosition(tableCode: String, player: Player, team: Team, position: Position): Match {
        return tableRepository.findByCode(tableCode)
            .let { matchRepository.findByTableCodeAndEndTimeNull(tableCode) ?: Match(it, LocalDateTime.now()) }
            .also { match ->
                if (isNewPlayer(match, player)) {
                    match.reservationStart = LocalDateTime.now()
                }
            }
            .also { match -> clearPlayerOldPosition(player, match) }
            .also { match -> setPlayerNewPosition(team, position, match, player) }
            .also { match ->
                if (allPositionsTaken(match)) {
                    match.startTime = LocalDateTime.now()
                }
            }
            .let { match -> matchRepository.save(match) }
    }

    @Cacheable("match-reservations")
    override fun getMatchReservations(): Set<Match> {
        return matchRepository.findAllByStartTimeNullAndEndTimeNullAndReservationStartNotNull()
    }

    @CacheEvict("match-reservations", allEntries = true)
    override fun delete(match: Match) {
        matchRepository.delete(match)
    }

    private fun isNewPlayer(match: Match, player: Player): Boolean {
        return !(player == match.playerAlphaAttacker
            || player == match.playerAlphaGoalkeeper
            || player == match.playerBetaAttacker
            || player == match.playerBetaGoalkeeper)
    }

    private fun allPositionsTaken(match: Match) =
        match.playerAlphaAttacker != null && match.playerAlphaGoalkeeper != null
            && match.playerBetaAttacker != null && match.playerBetaGoalkeeper != null

    private fun setPlayerNewPosition(team: Team, position: Position, match: Match, player: Player) {
        when (team) {
            Team.ALPHA -> when (position) {
                Position.ATTACKER -> match.playerAlphaAttacker = player
                Position.GOALKEEPER -> match.playerAlphaGoalkeeper = player
            }
            Team.BETA -> when (position) {
                Position.ATTACKER -> match.playerBetaAttacker = player
                Position.GOALKEEPER -> match.playerBetaGoalkeeper = player
            }
        }
    }

    private fun clearPlayerOldPosition(player: Player, match: Match) {
        if (player == match.playerAlphaAttacker) {
            match.playerAlphaAttacker = null
        }
        if (player == match.playerAlphaGoalkeeper) {
            match.playerAlphaGoalkeeper = null
        }
        if (player == match.playerBetaAttacker) {
            match.playerBetaAttacker = null
        }
        if (player == match.playerBetaGoalkeeper) {
            match.playerBetaGoalkeeper = null
        }
    }

    override fun score(tableCode: String, team: Team, position: Position?): Match? {
        return matchRepository.findByTableCodeAndStartTimeNotNullAndEndTimeNull(tableCode)
            ?.let { match ->
                when (team) {
                    Team.ALPHA -> {
                        match.teamAlphaScore++
                        when (position) {
                            Position.ATTACKER -> match.playerAlphaAttackerScore++
                            Position.GOALKEEPER -> match.playerAlphaGoalkeeperScore++
                        }
                    }
                    Team.BETA -> {
                        match.teamBetaScore++
                        when (position) {
                            Position.ATTACKER -> match.playerBetaAttackerScore++
                            Position.GOALKEEPER -> match.playerBetaGoalkeeperScore++
                        }
                    }
                }
                if (match.teamAlphaScore == 10 || match.teamBetaScore == 10) {
                    match.endTime = LocalDateTime.now()
                }
                return@let match
            }
    }

    override fun clearPosition(tableCode: String, team: Team, position: Position): Match? {
        return matchRepository.findByTableCodeAndEndTimeNull(tableCode)
            ?.let { match ->
                when (team) {
                    Team.ALPHA -> when (position) {
                        Position.ATTACKER -> match.playerAlphaAttacker = null
                        Position.GOALKEEPER -> match.playerAlphaGoalkeeper = null
                    }
                    Team.BETA -> when (position) {
                        Position.ATTACKER -> match.playerBetaAttacker = null
                        Position.GOALKEEPER -> match.playerBetaGoalkeeper = null
                    }
                }
                return@let match
            }
    }

    override fun switchPositions(tableCode: String, team: Team): Match? {
        return matchRepository.findByTableCodeAndEndTimeNull(tableCode)
            ?.let { match ->
                when (team) {
                    Team.ALPHA -> {
                        val attacker = match.playerAlphaAttacker
                        match.playerAlphaAttacker = match.playerAlphaGoalkeeper
                        match.playerAlphaGoalkeeper = attacker
                    }
                    Team.BETA -> {
                        val attacker = match.playerBetaAttacker
                        match.playerBetaAttacker = match.playerBetaGoalkeeper
                        match.playerBetaGoalkeeper = attacker
                    }
                }
                return@let match
            }
    }
}
