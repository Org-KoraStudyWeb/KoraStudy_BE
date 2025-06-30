package korastudy.be.config;

import korastudy.be.security.jwt.JwtEntryPoint;
import korastudy.be.security.jwt.JwtFilter;
import korastudy.be.security.userprinciple.AccountDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

    private final JwtEntryPoint jwtEntryPoint;
    private final JwtFilter jwtFilter;
    private final AccountDetailsServiceImpl accountDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()).cors(cors -> {
                }) // cấu hình CORS nếu cần
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtEntryPoint))
                .authorizeHttpRequests(auth -> auth.requestMatchers("/api/v1/auth/**",         // Login, Register
                                "/api/v1/public/**",       // Trang chủ, trang tĩnh
                                "/swagger-ui/**",
                                "/v3/api-docs/**")
                        .permitAll()

                        // Các route dành cho user thông thường
                        .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")

                        // Các route dành cho admin hệ thống
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Các route dành cho content manager
                        .requestMatchers("/api/content/**").hasRole("CONTENT_MANAGER")

                        // Các route dành cho delivery manager
                        .requestMatchers("/api/delivery/**").hasRole("DELIVERY_MANAGER")

                        // Mặc định: phải xác thực
                        .anyRequest().authenticated()).authenticationProvider(authenticationProvider()).addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // rounds = 12
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