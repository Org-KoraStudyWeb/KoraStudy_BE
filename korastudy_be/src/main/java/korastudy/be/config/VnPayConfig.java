package korastudy.be.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "vnpay")
@Getter
@Setter
public class VnPayConfig {
    private String payUrl;
    private String returnUrl;
    private String tmnCode;
    private String hashSecret;
    private String apiUrl;
    private String version;
    private String command;
}
