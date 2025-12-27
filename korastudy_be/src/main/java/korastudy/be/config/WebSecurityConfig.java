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
import org.springframework.web.cors.CorsConfiguration;

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
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.addAllowedOriginPattern("*");
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(List.of("*"));
                    config.setAllowCredentials(true);
                    return config;
                }))
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtEntryPoint))
                .authorizeHttpRequests(auth -> auth
                        // ========== PUBLIC ENDPOINTS (Không cần auth) ==========
                        .requestMatchers(
                                "/api/v1/auth/**",  // Tất cả auth endpoints
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/error",
                                "/favicon.ico",
                                "/.well-known/**",
                                "/ws/**",
                                "/ws/info",
                                "/api/v1/payments/vnpay-return",
                                "/api/v1/payments/callback",
                                "/api/v1/payments/notify",
                                "/api/v1/certificates/public/verify/**",
                                "/api/flashcards/system"
                        ).permitAll()

                        // ========== PUBLIC REVIEW ENDPOINTS (GET) ==========
                        // Review courses - public GET
                        .requestMatchers(HttpMethod.GET, "/api/v1/reviews/courses/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/reviews/courses/*/average-rating").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/reviews/courses/*/count").permitAll()

                        // Review mock tests - public GET
                        .requestMatchers(HttpMethod.GET, "/api/v1/reviews/mock-tests/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/reviews/mock-tests/*/average-rating").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/reviews/mock-tests/*/count").permitAll()

                        // ========== PUBLIC COURSES ENDPOINTS ==========
                        .requestMatchers(HttpMethod.GET, "/api/v1/courses/**").permitAll()

                        // ========== PRIVATE REVIEW ENDPOINTS (Cần auth) ==========
                        // Tạo review - cần auth
                        .requestMatchers(HttpMethod.POST, "/api/v1/reviews").authenticated()
                        // Sửa review - cần auth
                        .requestMatchers(HttpMethod.PUT, "/api/v1/reviews/**").authenticated()
                        // Xóa review - cần auth
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/reviews/**").authenticated()
                        // Lấy review của tôi - cần auth
                        .requestMatchers(HttpMethod.GET, "/api/v1/reviews/my-reviews").authenticated()
                        // Kiểm tra đã review chưa - cần auth
                        .requestMatchers(HttpMethod.GET, "/api/v1/reviews/check").authenticated()
                        // Lấy chi tiết review (cho owner) - cần auth
                        .requestMatchers(HttpMethod.GET, "/api/v1/reviews/{id}").authenticated()

                        // ========== ADMIN ENDPOINTS ==========
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // ========== PAYMENT ENDPOINTS ==========
                        .requestMatchers(HttpMethod.POST, "/api/v1/payments/create").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/payments/history").authenticated()

                        // ========== DEFAULT ==========
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

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