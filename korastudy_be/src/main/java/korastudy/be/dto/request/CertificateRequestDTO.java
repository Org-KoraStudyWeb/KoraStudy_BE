package korastudy.be.dto.request;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificateRequestDTO {
    private String certificateName;
    private LocalDate certificateDate;
    private Long userId;
    private Long courseId;
}
