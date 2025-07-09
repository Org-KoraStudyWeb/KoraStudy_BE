package korastudy.be.dto.response;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificateResponseDTO {
    private Long id;
    private String certificateName;
    private LocalDate certificateDate;
    private Long userId;
    private Long courseId;
}

