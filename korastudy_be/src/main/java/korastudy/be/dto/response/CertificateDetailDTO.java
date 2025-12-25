package korastudy.be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateDetailDTO {
    private Long id;
    private String certificateCode;
    private String certificateName;
    private LocalDate certificateDate;
    private String grade;
    private Double averageScore;

    // User info
    private String userName;
    private String userEmail;

    // Course info
    private String courseName;
    private String courseDescription;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Additional info
    private String qrCodeUrl; // URL để generate QR code
    private String verificationUrl; // URL xác thực certificate
}