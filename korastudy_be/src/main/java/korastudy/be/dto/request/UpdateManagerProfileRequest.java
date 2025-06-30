package korastudy.be.dto.request;

import korastudy.be.entity.Enum.Gender;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateManagerProfileRequest {
    private String firstName;
    private String lastName;
    private String myEmail;
    private String phone;
    private String idCard;
    private LocalDate dob;
    private Gender gender;
    private String avatar;
}
