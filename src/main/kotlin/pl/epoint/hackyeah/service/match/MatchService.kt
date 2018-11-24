package pl.epoint.hackyeah.service.match

import com.sun.xml.internal.fastinfoset.alphabet.BuiltInRestrictedAlphabets.table
import org.springframework.stereotype.Service
import pl.epoint.hackyeah.domain.Match
import pl.epoint.hackyeah.domain.Player
import pl.epoint.hackyeah.repository.FoosballTableRepository
import pl.epoint.hackyeah.repository.MatchRepository
import sun.audio.AudioPlayer.player

/**
 * @author Piotr Wolny
 */
@Service
class MatchService(val tableRepository: FoosballTableRepository,
                   val matchRepository: MatchRepository) {

    fun getMatch(tableCode: String): Match? {
        return matchRepository.findByTableCode(tableCode)
    }

    fun takePosition(tableCode: String, player: Player, team: Team, position: Position): Match {
        val table = tableRepository.findByCode(tableCode)!!
        var match = matchRepository.findByTableCode(tableCode)
        if (match == null) {
            match = Match(table)
        }
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
        return match
    }

    fun score(tableCode: String, team: Team, position: Position?): Match {
        val match = matchRepository.findByTableCode(tableCode)!!
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
        return match
    }

    fun clearPosition(tableCode: String, team: Team, position: Position): Match {
        val match = matchRepository.findByTableCode(tableCode)!!
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
        return match
    }

    fun switchPositions(tableCode: String, team: Team): Match {
        val match = matchRepository.findByTableCode(tableCode)!!
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
        return match
    }
}
