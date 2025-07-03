package korastudy.be.dto.request;

import korastudy.be.entity.Enum.Gender;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserProfileUpdate {
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Gender gender;
    private String avatar;
    private LocalDate dateOfBirth;
}
