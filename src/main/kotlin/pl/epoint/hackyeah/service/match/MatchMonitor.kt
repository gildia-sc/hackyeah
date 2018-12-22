package pl.epoint.hackyeah.service.match

import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import pl.epoint.hackyeah.web.rest.MatchDto
import java.time.Duration
import java.time.LocalDateTime

@Component
class MatchMonitor(private val matchService: MatchService,
                   private val simpMessagingTemplate: SimpMessagingTemplate) {

    @Scheduled(fixedRate = 1000L)
    fun monitorPendingMatches() {
        matchService.getMatchReservations()
            .filter { match ->
                val reservationStart = match.reservationStart!!
                val currentTime = LocalDateTime.now()
                val reservationDate = reservationStart.toLocalDate()
                val reservationTime = reservationStart.toLocalTime()
                reservationDate.isBefore(currentTime.toLocalDate())
                    || Duration.between(reservationTime, currentTime.toLocalTime()).seconds >= 60
            }.forEach { match ->
                matchService.delete(match)
                simpMessagingTemplate.convertAndSend(match.table.code, MatchDto(matchService.getCurrentMatch(match.table.code)))
            }
    }

}
