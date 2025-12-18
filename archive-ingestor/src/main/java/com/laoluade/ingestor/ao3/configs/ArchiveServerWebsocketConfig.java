package com.laoluade.ingestor.ao3.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

@Configuration
@EnableWebSocketMessageBroker
public class ArchiveServerWebsocketConfig {
    public void registerStompEndpoints(StompEndpointRegistry websocketRegistry,
                                       @Value("${server.port:8080}") Integer port) {
        websocketRegistry.addEndpoint("/api/v1/websocket")
                .setAllowedOriginPatterns("http://localhost:" + port)
                .withSockJS();
    }

    public void configureMessageBroker(MessageBrokerRegistry brokerRegistry) {
        brokerRegistry.enableSimpleBroker("/api/v1/websocket/topic");
        brokerRegistry.setApplicationDestinationPrefixes("/api/v1/websocket/app");
    }
}
