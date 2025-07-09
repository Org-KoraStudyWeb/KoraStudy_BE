package korastudy.be.service.impl;

import korastudy.be.dto.request.CertificateRequestDTO;
import korastudy.be.dto.response.CertificateResponseDTO;
import korastudy.be.entity.Certificate;
import korastudy.be.entity.Course.Course;
import korastudy.be.entity.User.User;
import korastudy.be.repository.CertificateRepository;
import korastudy.be.repository.CourseRepository;
import korastudy.be.repository.UserRepository;
import korastudy.be.service.ICertificateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CertificateService implements ICertificateService {

    private final CertificateRepository certificateRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @Override
    public CertificateResponseDTO addCertificate(CertificateRequestDTO dto) {
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học"));

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        if (certificateRepository.findByCourseId(course.getId()).isPresent()) {
            throw new RuntimeException("Khóa học này có thể thêm chứng chỉ");
        }

        Certificate cert = Certificate.builder()
                .certificateName(dto.getCertificateName())
                .certificateDate(dto.getCertificateDate())
                .course(course)
                .user(user)
                .build();

        certificateRepository.save(cert);

        return CertificateResponseDTO.builder()
                .id(cert.getId())
                .certificateName(cert.getCertificateName())
                .certificateDate(cert.getCertificateDate())
                .userId(user.getId())
                .courseId(course.getId())
                .build();
    }


    @Override
    public CertificateResponseDTO getCertificateByCourse(Long courseId) {
        Certificate cert = certificateRepository.findByCourseId(courseId)
                .orElseThrow(() -> new RuntimeException("Certificate not found"));
        return CertificateResponseDTO.builder()
                .id(cert.getId())
                .certificateName(cert.getCertificateName())
                .certificateDate(cert.getCertificateDate())
                .userId(cert.getUser().getId())
                .courseId(cert.getCourse().getId())
                .build();
    }
}

