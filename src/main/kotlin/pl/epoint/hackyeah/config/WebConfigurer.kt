package pl.epoint.hackyeah.config

import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.servlet.InstrumentedFilter
import com.codahale.metrics.servlets.MetricsServlet
import io.github.jhipster.config.JHipsterConstants
import io.github.jhipster.config.JHipsterProperties
import io.github.jhipster.web.filter.CachingHttpHeadersFilter
import io.undertow.UndertowOptions
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.embedded.undertow.UndertowBuilderCustomizer
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory
import org.springframework.boot.web.server.MimeMappings
import org.springframework.boot.web.server.WebServerFactory
import org.springframework.boot.web.server.WebServerFactoryCustomizer
import org.springframework.boot.web.servlet.ServletContextInitializer
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.resource.PathResourceResolver

import javax.servlet.DispatcherType
import javax.servlet.ServletContext
import javax.servlet.ServletException
import java.io.File
import java.io.UnsupportedEncodingException
import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import java.util.EnumSet

import java.net.URLDecoder.decode

/**
 * Configuration of web application with Servlet 3.0 APIs.
 */
@Configuration
class WebConfigurer(private val env: Environment,
                    private val jHipsterProperties: JHipsterProperties) :
    ServletContextInitializer, WebServerFactoryCustomizer<WebServerFactory>, WebMvcConfigurer {

    private val log = LoggerFactory.getLogger(WebConfigurer::class.java)
    private var metricRegistry: MetricRegistry? = null

    @Throws(ServletException::class)
    override fun onStartup(servletContext: ServletContext) {
        if (env.activeProfiles.isNotEmpty()) {
            log.info("Web application configuration, using profiles: {}", *env.activeProfiles as Array<Any>)
        }
        val disps = EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.ASYNC)
        initMetrics(servletContext, disps)
        if (env.acceptsProfiles(JHipsterConstants.SPRING_PROFILE_PRODUCTION)) {
            initCachingHttpHeadersFilter(servletContext, disps)
        }
        log.info("Web application fully configured")
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/**/*")
            .addResourceLocations("classpath:/static/")
            .resourceChain(true)
            .addResolver(IndexPathResourceResolver())
    }

    /**
     * Customize the Servlet engine: Mime types, the document root, the cache.
     */
    override fun customize(server: WebServerFactory) {
        setMimeMappings(server)
        // When running in an IDE or with ./gradlew bootRun, set location of the static web assets.
        setLocationForStaticAssets(server)

        /*
         * Enable HTTP/2 for Undertow - https://twitter.com/ankinson/status/829256167700492288
         * HTTP/2 requires HTTPS, so HTTP requests will fallback to HTTP/1.1.
         * See the JHipsterProperties class and your application-*.yml configuration files
         * for more information.
         */
        if (jHipsterProperties.http.getVersion() == JHipsterProperties.Http.Version.V_2_0 && server is UndertowServletWebServerFactory) {

            server
                    .addBuilderCustomizers(UndertowBuilderCustomizer{ builder -> builder.setServerOption(UndertowOptions.ENABLE_HTTP2, true) })
        }
    }

    private fun setMimeMappings(server: WebServerFactory) {
        if (server is ConfigurableServletWebServerFactory) {
            val mappings = MimeMappings(MimeMappings.DEFAULT)
            // IE issue, see https://github.com/jhipster/generator-jhipster/pull/711
            mappings.add("html", MediaType.TEXT_HTML_VALUE + ";charset=" + StandardCharsets.UTF_8.name().toLowerCase())
            // CloudFoundry issue, see https://github.com/cloudfoundry/gorouter/issues/64
            mappings.add("json", MediaType.TEXT_HTML_VALUE + ";charset=" + StandardCharsets.UTF_8.name().toLowerCase())
            server.setMimeMappings(mappings)
        }
    }

    private fun setLocationForStaticAssets(server: WebServerFactory) {
        if (server is ConfigurableServletWebServerFactory) {
            val root: File
            val prefixPath = resolvePathPrefix()
            root = File(prefixPath + "build/www/")
            if (root.exists() && root.isDirectory) {
                server.setDocumentRoot(root)
            }
        }
    }

    /**
     * Resolve path prefix to static resources.
     */
    private fun resolvePathPrefix(): String {
        var fullExecutablePath: String
        try {
            fullExecutablePath = decode(this.javaClass.getResource("").path, StandardCharsets.UTF_8.name())
        } catch (e: UnsupportedEncodingException) {
            /* try without decoding if this ever happens */
            fullExecutablePath = this.javaClass.getResource("").path
        }

        val rootPath = Paths.get(".").toUri().normalize().path
        val extractedPath = fullExecutablePath.replace(rootPath, "")
        val extractionEndIndex = extractedPath.indexOf("build/")
        return if (extractionEndIndex <= 0) {
            ""
        } else extractedPath.substring(0, extractionEndIndex)
    }

    /**
     * Initializes the caching HTTP Headers Filter.
     */
    private fun initCachingHttpHeadersFilter(servletContext: ServletContext,
                                             disps: EnumSet<DispatcherType>) {
        log.debug("Registering Caching HTTP Headers Filter")
        val cachingHttpHeadersFilter = servletContext.addFilter("cachingHttpHeadersFilter",
                CachingHttpHeadersFilter(jHipsterProperties))

        cachingHttpHeadersFilter.addMappingForUrlPatterns(disps, true, "/i18n/*")
        cachingHttpHeadersFilter.addMappingForUrlPatterns(disps, true, "/content/*")
        cachingHttpHeadersFilter.addMappingForUrlPatterns(disps, true, "/app/*")
        cachingHttpHeadersFilter.setAsyncSupported(true)
    }

    /**
     * Initializes Metrics.
     */
    private fun initMetrics(servletContext: ServletContext, disps: EnumSet<DispatcherType>) {
        log.debug("Initializing Metrics registries")
        servletContext.setAttribute(InstrumentedFilter.REGISTRY_ATTRIBUTE,
                metricRegistry)
        servletContext.setAttribute(MetricsServlet.METRICS_REGISTRY,
                metricRegistry)

        log.debug("Registering Metrics Filter")
        val metricsFilter = servletContext.addFilter("webappMetricsFilter",
                InstrumentedFilter())

        metricsFilter.addMappingForUrlPatterns(disps, true, "/*")
        metricsFilter.setAsyncSupported(true)

        log.debug("Registering Metrics Servlet")
        val metricsAdminServlet = servletContext.addServlet("metricsServlet", MetricsServlet())

        metricsAdminServlet.addMapping("/management/metrics/*")
        metricsAdminServlet.setAsyncSupported(true)
        metricsAdminServlet.setLoadOnStartup(2)
    }

    @Bean
    fun corsFilter(): CorsFilter {
        val source = UrlBasedCorsConfigurationSource()
        val config = jHipsterProperties.cors
        if (config.allowedOrigins != null && !config.allowedOrigins!!.isEmpty()) {
            log.debug("Registering CORS filter")
            source.registerCorsConfiguration("/api/**", config)
            source.registerCorsConfiguration("/management/**", config)
            source.registerCorsConfiguration("/v2/api-docs", config)
        }
        return CorsFilter(source)
    }

    @Autowired(required = false)
    fun setMetricRegistry(metricRegistry: MetricRegistry) {
        this.metricRegistry = metricRegistry
    }
}

private class IndexPathResourceResolver: PathResourceResolver() {

    override fun getResource(resourcePath: String, location: Resource): Resource? {
        val requestedResource = location.createRelative(resourcePath)
        return if (requestedResource.exists() && requestedResource.isReadable) {
            requestedResource
        } else {
            ClassPathResource("/static/index.html")
        }
    }
}
