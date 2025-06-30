package korastudy.be.dto.request;

import lombok.*;

@Data
public class LoginRequest {
    private String username;
    private String password;
    private String email;
}
