package com.laoluade.ingestor.ao3.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * <p>This class defines the websocket configuration for the Archive Server application.</p>
 * <p>This class implements {@link WebSocketMessageBrokerConfigurer} to configure a STOMP broker and a message broker.</p>
 * <p>This class uses the following settings from the application.properties file to configure itself:</p>
 *  <ul>
 *      <li>archiveServer.websocket.port</li>
 *  </ul>
 *  <p>All class attributes correspond to their <code>archiveServer.websocket</code> counterpart.</p>
 */
@Configuration
@EnableWebSocketMessageBroker
public class ArchiveServerWebsocketConfig implements WebSocketMessageBrokerConfigurer {
    /**
     * <p>This attribute specifies the port where the websocket should run on.</p>
     */
    @Value("${archiveServer.websocket.port}")
    private Integer port;

    /**
     * <p>This method registers webpage URIs as places to subscribe to the STOMP websocket.</p>
     * @param websocketRegistry The {@link StompEndpointRegistry} to modify.
     */
    public void registerStompEndpoints(StompEndpointRegistry websocketRegistry) {
        websocketRegistry.addEndpoint("/api/v1/websocket")
                .setAllowedOriginPatterns("http://localhost:" + this.port + "*");
    }

    /**
     * <p>This method configures where the message broker can be accessed and where the message broker publish messages.</p>
     * @param brokerRegistry The {@link MessageBrokerRegistry} to configure.
     */
    public void configureMessageBroker(MessageBrokerRegistry brokerRegistry) {
        brokerRegistry.enableSimpleBroker("/api/v1/websocket/topic");
        brokerRegistry.setApplicationDestinationPrefixes("/api/v1/websocket/app");
    }
}
