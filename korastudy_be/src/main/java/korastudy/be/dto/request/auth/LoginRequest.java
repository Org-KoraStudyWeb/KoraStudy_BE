package korastudy.be.dto.request.auth;

import lombok.*;

@Data
public class LoginRequest {
    private String username;
    private String password;
}
