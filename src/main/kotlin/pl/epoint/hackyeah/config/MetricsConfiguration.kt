package pl.epoint.hackyeah.config

import com.codahale.metrics.JmxReporter
import com.codahale.metrics.JvmAttributeGaugeSet
import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.Slf4jReporter
import com.codahale.metrics.health.HealthCheckRegistry
import com.codahale.metrics.jvm.BufferPoolMetricSet
import com.codahale.metrics.jvm.FileDescriptorRatioGauge
import com.codahale.metrics.jvm.GarbageCollectorMetricSet
import com.codahale.metrics.jvm.MemoryUsageGaugeSet
import com.codahale.metrics.jvm.ThreadStatesGaugeSet
import com.ryantenney.metrics.spring.config.annotation.EnableMetrics
import com.ryantenney.metrics.spring.config.annotation.MetricsConfigurerAdapter
import com.zaxxer.hikari.HikariDataSource
import io.github.jhipster.config.JHipsterProperties
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.dropwizard.DropwizardExports
import io.prometheus.client.exporter.MetricsServlet
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.servlet.ServletContextInitializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import javax.annotation.PostConstruct
import javax.servlet.ServletContext
import java.lang.management.ManagementFactory
import java.util.concurrent.TimeUnit

@Configuration
@EnableMetrics(proxyTargetClass = true)
class MetricsConfiguration(private val jHipsterProperties: JHipsterProperties) : MetricsConfigurerAdapter(), ServletContextInitializer {

    private val log = LoggerFactory.getLogger(MetricsConfiguration::class.java)

    private val metricRegistry = MetricRegistry()

    private val healthCheckRegistry = HealthCheckRegistry()

    private var hikariDataSource: HikariDataSource? = null

    @Autowired(required = false)
    fun setHikariDataSource(hikariDataSource: HikariDataSource) {
        this.hikariDataSource = hikariDataSource
    }

    @Bean
    override fun getMetricRegistry(): MetricRegistry {
        return metricRegistry
    }

    @Bean
    override fun getHealthCheckRegistry(): HealthCheckRegistry {
        return healthCheckRegistry
    }

    @PostConstruct
    fun init() {
        log.debug("Registering JVM gauges")
        metricRegistry.register(PROP_METRIC_REG_JVM_MEMORY, MemoryUsageGaugeSet())
        metricRegistry.register(PROP_METRIC_REG_JVM_GARBAGE, GarbageCollectorMetricSet())
        metricRegistry.register(PROP_METRIC_REG_JVM_THREADS, ThreadStatesGaugeSet())
        metricRegistry.register(PROP_METRIC_REG_JVM_FILES, FileDescriptorRatioGauge())
        metricRegistry.register(PROP_METRIC_REG_JVM_BUFFERS, BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer()))
        metricRegistry.register(PROP_METRIC_REG_JVM_ATTRIBUTE_SET, JvmAttributeGaugeSet())
        if (hikariDataSource != null) {
            log.debug("Monitoring the datasource")
            // remove the factory created by HikariDataSourceMetricsPostProcessor until JHipster migrate to Micrometer
            hikariDataSource!!.metricsTrackerFactory = null
            hikariDataSource!!.metricRegistry = metricRegistry
        }
        if (jHipsterProperties.metrics.jmx.isEnabled) {
            log.debug("Initializing Metrics JMX reporting")
            val jmxReporter = JmxReporter.forRegistry(metricRegistry).build()
            jmxReporter.start()
        }
        if (jHipsterProperties.metrics.logs.isEnabled) {
            log.info("Initializing Metrics Log reporting")
            val metricsMarker = MarkerFactory.getMarker("metrics")
            val reporter = Slf4jReporter.forRegistry(metricRegistry)
                    .outputTo(LoggerFactory.getLogger("metrics"))
                    .markWith(metricsMarker)
                    .convertRatesTo(TimeUnit.SECONDS)
                    .convertDurationsTo(TimeUnit.MILLISECONDS)
                    .build()
            reporter.start(jHipsterProperties.metrics.logs.reportFrequency, TimeUnit.SECONDS)
        }
    }

    override fun onStartup(servletContext: ServletContext) {

        if (jHipsterProperties.metrics.prometheus.isEnabled) {
            val endpoint = jHipsterProperties.metrics.prometheus.endpoint

            log.debug("Initializing prometheus metrics exporting via {}", endpoint)

            CollectorRegistry.defaultRegistry.register(DropwizardExports(metricRegistry))
            servletContext
                    .addServlet("prometheusMetrics", MetricsServlet(CollectorRegistry.defaultRegistry))
                    .addMapping(endpoint)
        }
    }

    companion object {

        private val PROP_METRIC_REG_JVM_MEMORY = "jvm.memory"
        private val PROP_METRIC_REG_JVM_GARBAGE = "jvm.garbage"
        private val PROP_METRIC_REG_JVM_THREADS = "jvm.threads"
        private val PROP_METRIC_REG_JVM_FILES = "jvm.files"
        private val PROP_METRIC_REG_JVM_BUFFERS = "jvm.buffers"
        private val PROP_METRIC_REG_JVM_ATTRIBUTE_SET = "jvm.attributes"
    }
}
