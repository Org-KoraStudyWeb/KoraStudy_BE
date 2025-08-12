package korastudy.be.config;

import korastudy.be.security.jwt.JwtUtils;
import korastudy.be.security.userprinciple.AccountDetailsServiceImpl;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtUtils jwtUtils;
    private final AccountDetailsServiceImpl accountDetailsService;
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Prefix cho các topic mà client sẽ subscribe
        registry.enableSimpleBroker("/topic", "/user");
        // Prefix cho các endpoint mà client sẽ gửi message tới
        registry.setApplicationDestinationPrefixes("/app");
        // Định nghĩa prefix cho user-specific messages
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor != null && accessor.getDestination() != null && 
                    (accessor.getDestination().contains("/ws/info") || 
                    accessor.getDestination().contains("/ws/websocket"))) {
                    return message;
                }   

                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String authorization = accessor.getFirstNativeHeader("Authorization");
                    if (authorization != null && authorization.startsWith("Bearer ")) {
                        try {
                            String token = authorization.substring(7);
                            // Thêm xử lý try-catch để bắt lỗi khi xác thực token
                            if (jwtUtils.validateJwtToken(token)) {
                                String username = jwtUtils.getUsernameFromJwtToken(token);
                                UserDetails userDetails = accountDetailsService.loadUserByUsername(username);
                                Authentication auth = new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities());
                                accessor.setUser(auth);
                            }
                        } catch (Exception e) {
                            // Log lỗi thay vì để nó ném ra exception
                            System.err.println("Error validating JWT token: " + e.getMessage());
                        }
                    }
                }
                return message;
            }
        });
    }
}