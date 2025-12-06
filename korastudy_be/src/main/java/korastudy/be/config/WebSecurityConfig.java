package korastudy.be.config;

import korastudy.be.security.jwt.JwtEntryPoint;
import korastudy.be.security.jwt.JwtFilter;
import korastudy.be.security.userprinciple.AccountDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

    private final JwtEntryPoint jwtEntryPoint;
    private final JwtFilter jwtFilter;
    private final AccountDetailsServiceImpl accountDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable).cors(cors -> cors.configurationSource(request -> {
            var config = new org.springframework.web.cors.CorsConfiguration();
            config.addAllowedOriginPattern("*"); // ✅ Cho phép mọi origin (đỡ lỗi FE)
            config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
            config.setAllowedHeaders(List.of("*"));
            config.setAllowCredentials(true);
            return config;
        })).sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).exceptionHandling(ex -> ex.authenticationEntryPoint(jwtEntryPoint)).authorizeHttpRequests(auth -> auth.requestMatchers("/api/v1/auth/**", "/swagger-ui/**", "/v3/api-docs/**", "/error", "/favicon.ico", "/.well-known/**", "/api/flashcards/system", "/ws/**", "/ws/info", "/api/v1/payments/vnpay-return", "/api/v1/payments/callback", "/api/v1/payments/notify", "/api/v1/auth/verify-email", "/api/v1/auth/resend-verification", "api/v1/auth/forgot-password").permitAll().requestMatchers(HttpMethod.GET, "/api/v1/courses/**").permitAll().requestMatchers(HttpMethod.POST, "/api/v1/payments/create").authenticated().requestMatchers(HttpMethod.GET, "/api/v1/payments/history").authenticated().anyRequest().authenticated()).authenticationProvider(authenticationProvider()).addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(accountDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}