package korastudy.be.dto.response.auth;

import korastudy.be.entity.Enum.Gender;
import korastudy.be.entity.User.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class UserManagementResponse {

    private Long id;
    private String userCode;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phoneNumber;
    private Gender gender;
    private LocalDate dateOfBirth;
    private String avatar;
    private String level;
    private String idCard;
    private Boolean isUserEnabled; // User.isEnable
    private Boolean isAccountEnabled; // Account.isEnabled
    private LocalDateTime createdAt;
    private LocalDateTime lastModified;
    
    // Account info
    private Long accountId;
    private List<String> roles;

    public static UserManagementResponse fromEntity(User user) {
        return UserManagementResponse.builder()
                .id(user.getId())
                .userCode(user.getUserCode())
                .username(user.getAccount() != null ? user.getAccount().getUsername() : null)
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(buildFullName(user.getFirstName(), user.getLastName()))
                .phoneNumber(user.getPhoneNumber())
                .gender(user.getGender())
                .dateOfBirth(user.getDob())
                .avatar(user.getAvatar())
                .level(user.getLevel())
                .idCard(user.getIdCard())
                .isUserEnabled(user.isEnable())
                .isAccountEnabled(user.getAccount() != null ? user.getAccount().isEnabled() : false)
                .createdAt(user.getCreatedAt())
                .lastModified(user.getLastModified())
                .accountId(user.getAccount() != null ? user.getAccount().getId() : null)
                .roles(user.getAccount() != null ? 
                       user.getAccount().getRoles().stream()
                           .map(role -> role.getRoleName().name())
                           .collect(Collectors.toList()) : null)
                .build();
    }

    private static String buildFullName(String firstName, String lastName) {
        if (firstName == null && lastName == null) return null;
        if (firstName == null) return lastName;
        if (lastName == null) return firstName;
        return firstName + " " + lastName;
    }
}