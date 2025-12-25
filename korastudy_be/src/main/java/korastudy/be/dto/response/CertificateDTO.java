package korastudy.be.dto.response;

import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateDTO {
    private Long id;
    private String certificateCode;
    private String certificateName;
    private LocalDate certificateDate;
    private String grade;
    private Double averageScore;

    // User info
    private Long userId;
    private String userName;

    // Course info
    private Long courseId;
    private String courseName;

    // URLs
    private String detailUrl;
    private String shareUrl;
    private String verifyUrl;
}