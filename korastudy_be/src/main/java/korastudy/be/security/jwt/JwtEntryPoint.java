package korastudy.be.security.jwt;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class JwtEntryPoint implements AuthenticationEntryPoint {

    /**
     * Gọi khi người dùng gửi request không có JWT hợp lệ
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();
        String token = request.getHeader("Authorization");
        String ip = request.getRemoteAddr();

        log.warn("""
                🔒 Unauthorized access attempt:
                  - URI: {}
                  - Method: {}
                  - Client IP: {}
                  - Token present: {}
                  - Reason: {}
                """, path, method, ip, token != null, authException.getMessage());

        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error: Unauthorized access");
    }
}
