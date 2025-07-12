package korastudy.be.controller;

import korastudy.be.dto.request.CertificateRequestDTO;
import korastudy.be.dto.response.CertificateResponseDTO;
import korastudy.be.payload.response.ApiSuccess;
import korastudy.be.service.impl.CertificateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/certificate")
@RequiredArgsConstructor
public class CertificateController {
    private final CertificateService certificateService;

    @PostMapping("/create")
    public ResponseEntity<ApiSuccess> add(@RequestBody CertificateRequestDTO dto) {
        certificateService.addCertificate(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiSuccess.of("Chứng chỉ thêm thành công"));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<CertificateResponseDTO> getByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(certificateService.getCertificateByCourse(courseId));
    }
}
