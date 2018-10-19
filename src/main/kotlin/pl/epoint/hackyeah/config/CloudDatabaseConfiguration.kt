package pl.epoint.hackyeah.config

import io.github.jhipster.config.JHipsterConstants
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.cloud.config.java.AbstractCloudConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

import javax.sql.DataSource


@Configuration
@Profile(JHipsterConstants.SPRING_PROFILE_CLOUD)
class CloudDatabaseConfiguration : AbstractCloudConfig() {

    private val log = LoggerFactory.getLogger(CloudDatabaseConfiguration::class.java)

    @Bean
    @ConfigurationProperties("spring.datasource.hikari")
    fun dataSource(): DataSource {
        log.info("Configuring JDBC datasource from a cloud provider")
        return connectionFactory().dataSource()
    }
}
