package hackyeah

import hackyeah.config.ApplicationProperties
import hackyeah.config.DefaultProfileUtil
import io.github.jhipster.config.JHipsterConstants
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.core.env.Environment
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.*
import javax.annotation.PostConstruct

@SpringBootApplication
@EnableConfigurationProperties(LiquibaseProperties::class, ApplicationProperties::class)
class HackyeahApp(private val env: Environment) {

    /**
     * Initializes hackyeah.
     *
     *
     * Spring profiles can be configured with a program argument --spring.profiles.active=your-active-profile
     *
     *
     * You can find more information on how profiles work with JHipster on [https://www.jhipster.tech/profiles/](https://www.jhipster.tech/profiles/).
     */
    @PostConstruct
    fun initApplication() {
        val activeProfiles = Arrays.asList(*env.activeProfiles)
        if (activeProfiles.contains(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT) && activeProfiles.contains(JHipsterConstants.SPRING_PROFILE_PRODUCTION)) {
            log.error("You have misconfigured your application! It should not run " + "with both the 'dev' and 'prod' profiles at the same time.")
        }
        if (activeProfiles.contains(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT) && activeProfiles.contains(JHipsterConstants.SPRING_PROFILE_CLOUD)) {
            log.error("You have misconfigured your application! It should not " + "run with both the 'dev' and 'cloud' profiles at the same time.")
        }
    }

    companion object {

        private val log = LoggerFactory.getLogger(HackyeahApp::class.java)

        /**
         * Main method, used to run the application.
         *
         * @param args the command line arguments
         */
        @JvmStatic
        fun main(args: Array<String>) {
            val app = SpringApplication(HackyeahApp::class.java)
            DefaultProfileUtil.addDefaultProfile(app)
            val env = app.run(*args).environment
            logApplicationStartup(env)
        }

        private fun logApplicationStartup(env: Environment) {
            var protocol = "http"
            if (env.getProperty("server.ssl.key-store") != null) {
                protocol = "https"
            }
            val serverPort = env.getProperty("server.port")
            var contextPath = env.getProperty("server.servlet.context-path")
            if (StringUtils.isBlank(contextPath)) {
                contextPath = "/"
            }
            var hostAddress = "localhost"
            try {
                hostAddress = InetAddress.getLocalHost().hostAddress
            } catch (e: UnknownHostException) {
                log.warn("The host name could not be determined, using `localhost` as fallback")
            }

            log.info("\n----------------------------------------------------------\n\t" +
                    "Application '{}' is running! Access URLs:\n\t" +
                    "Local: \t\t{}://localhost:{}{}\n\t" +
                    "External: \t{}://{}:{}{}\n\t" +
                    "Profile(s): \t{}\n----------------------------------------------------------",
                    env.getProperty("spring.application.name"),
                    protocol,
                    serverPort,
                    contextPath,
                    protocol,
                    hostAddress,
                    serverPort,
                    contextPath,
                    env.activeProfiles)
        }
    }
}
