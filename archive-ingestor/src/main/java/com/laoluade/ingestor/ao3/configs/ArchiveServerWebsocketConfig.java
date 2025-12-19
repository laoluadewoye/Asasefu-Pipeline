package com.laoluade.ingestor.ao3.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class ArchiveServerWebsocketConfig implements WebSocketMessageBrokerConfigurer {
    @Value("${archiveServer.websocket.port:8080}")
    private Integer port;

    public void registerStompEndpoints(StompEndpointRegistry websocketRegistry) {
        websocketRegistry.addEndpoint("/api/v1/websocket")
                .setAllowedOriginPatterns("http://localhost:" + this.port + "*");
    }

    public void configureMessageBroker(MessageBrokerRegistry brokerRegistry) {
        brokerRegistry.enableSimpleBroker("/api/v1/websocket/topic");
        brokerRegistry.setApplicationDestinationPrefixes("/api/v1/websocket/app");
    }
}
