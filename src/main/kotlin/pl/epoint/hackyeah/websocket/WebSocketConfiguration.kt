package pl.epoint.hackyeah.websocket

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
import pl.epoint.hackyeah.repository.FoosballTableRepository

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfiguration(private val tableRepository: FoosballTableRepository) : WebSocketMessageBrokerConfigurer {

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint(DEFAULT_ENDPOINT_PATH).withSockJS()
    }

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        val paths = tableRepository.findAll()
            .map { it.code }
            .toTypedArray()
        LOG.info("{}", paths)
        registry.enableSimpleBroker(*paths)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(WebSocketConfiguration::class.java)
        private const val DEFAULT_ENDPOINT_PATH = "/api/ws"
    }
}
