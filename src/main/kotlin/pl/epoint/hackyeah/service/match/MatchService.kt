package pl.epoint.hackyeah.service.match

import pl.epoint.hackyeah.domain.Match
import pl.epoint.hackyeah.domain.Player

interface MatchService {

    /**
     * Swaps players' positions in the given team.
     */
    fun switchPositions(tableCode: String, team: Team): Match?

    /**
     * Clears given position in the team.
     */
    fun clearPosition(tableCode: String, team: Team, position: Position): Match?

    /**
     * Increments goal count for the given team. Optionally position information can be passed
     * to update player's statistics.
     */
    fun score(tableCode: String, team: Team, position: Position?): Match?

    /**
     * Puts a player on the given position in a team.
     */
    fun takePosition(tableCode: String, player: Player, team: Team, position: Position): Match

    /**
     * Returns current match, if exists.
     */
    fun getCurrentMatch(tableCode: String): Match

    /**
     * Returns matches with at least one player, which has not started yet.
     */
    fun getMatchReservations(): Set<Match>

    /**
     * Deletes the given match from the persistent storage
     */
    fun delete(match: Match)

    /**
     * Resets a match on the given table. If there's a match being played, it's aborted and new one is created in it's place.
     *
     */
    fun resetMatch(tableCode: String): Match
}
