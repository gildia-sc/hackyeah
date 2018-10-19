package pl.epoint.hackyeah.web.rest

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import com.codahale.metrics.annotation.Timed
import pl.epoint.hackyeah.web.rest.vm.LoggerVM
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.stream.Collectors

/**
 * Controller for view and managing Log Level at runtime.
 */
@RestController
@RequestMapping("/management")
class LogsResource {

    val list: List<LoggerVM>
        @GetMapping("/logs")
        @Timed
        get() {
            val context = LoggerFactory.getILoggerFactory() as LoggerContext
            return context.loggerList
                    .stream()
                    .map { LoggerVM(it) }
                    .collect(Collectors.toList())
        }

    @PutMapping("/logs")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Timed
    fun changeLevel(@RequestBody jsonLogger: LoggerVM) {
        val context = LoggerFactory.getILoggerFactory() as LoggerContext
        context.getLogger(jsonLogger.name!!).level = Level.valueOf(jsonLogger.level)
    }
}
