package pl.epoint.hackyeah.config

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement


@Configuration
@EnableJpaRepositories("pl.epoint.hackyeah.repository")
@EnableJpaAuditing(auditorAwareRef = "springSecurityAuditorAware")
@EnableTransactionManagement
class DatabaseConfiguration {

    private val log = LoggerFactory.getLogger(DatabaseConfiguration::class.java)
}
