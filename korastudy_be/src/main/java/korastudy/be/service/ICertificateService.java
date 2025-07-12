package korastudy.be.service;

import korastudy.be.dto.request.CertificateRequestDTO;
import korastudy.be.dto.response.CertificateResponseDTO;

public interface ICertificateService {
    CertificateResponseDTO addCertificate(CertificateRequestDTO dto);
    CertificateResponseDTO getCertificateByCourse(Long courseId);
}