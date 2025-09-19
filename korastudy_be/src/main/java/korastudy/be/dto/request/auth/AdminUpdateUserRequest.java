package korastudy.be.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import korastudy.be.entity.Enum.Gender;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AdminUpdateUserRequest {

    private String firstName;
    
    private String lastName;
    
    @Email(message = "Email không hợp lệ")
    private String email;
    
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại không hợp lệ")
    private String phoneNumber;
    
    private Gender gender;
    
    private LocalDate dateOfBirth;
    
    private String avatar;
    
    private String level;
    
    private String idCard;
    
    // Admin có thể update username (với validation)
    private String username;
}