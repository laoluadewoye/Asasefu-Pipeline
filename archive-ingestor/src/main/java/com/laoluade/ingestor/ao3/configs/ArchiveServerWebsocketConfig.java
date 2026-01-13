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
 *      <li>archiveServer.websocket.endpointURL</li>
 *      <li>archiveServer.websocket.topicURL</li>
 *      <li>archiveServer.websocket.appURL</li>
 *      <li>server.port</li>
 *  </ul>
 *  <p>All class attributes correspond to their <code>archiveServer.websocket</code> and <code>server</code> counterparts.</p>
 */
@Configuration
@EnableWebSocketMessageBroker
public class ArchiveServerWebsocketConfig implements WebSocketMessageBrokerConfigurer {
    /**
     * <p>This attribute specifies the relative URL of the websocket registry.</p>
     */
    @Value("${archiveServer.websocket.endpointURL}")
    private String endpointURL;

    /**
     * <p>This attribute specifies the relative URL of websocket topics.</p>
     */
    @Value("${archiveServer.websocket.topicURL}")
    private String topicURL;

    /**
     * <p>This attribute specifies the relative URL of websocket APIs.</p>
     */
    @Value("${archiveServer.websocket.appURL}")
    private String appURL;

    /**
     * <p>This attribute specifies the port where the websocket should run on.</p>
     */
    @Value("${server.port}")
    private Integer port;

    /**
     * <p>This method registers webpage URIs as places to subscribe to the STOMP websocket.</p>
     * @param websocketRegistry The {@link StompEndpointRegistry} to modify.
     */
    public void registerStompEndpoints(StompEndpointRegistry websocketRegistry) {
        String selfOriginPattern = "http://localhost:" + this.port;
        String angularDevOriginPattern = "http://localhost:4200";
        websocketRegistry.addEndpoint(this.endpointURL)
                .setAllowedOriginPatterns(selfOriginPattern, angularDevOriginPattern);
    }

    /**
     * <p>This method configures where the message broker can be accessed and where the message broker publish messages.</p>
     * @param brokerRegistry The {@link MessageBrokerRegistry} to configure.
     */
    public void configureMessageBroker(MessageBrokerRegistry brokerRegistry) {
        brokerRegistry.enableSimpleBroker(this.topicURL);
        brokerRegistry.setApplicationDestinationPrefixes(this.appURL);
    }
}
