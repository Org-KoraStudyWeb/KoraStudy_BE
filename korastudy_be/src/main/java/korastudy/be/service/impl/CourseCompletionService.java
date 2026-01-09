package korastudy.be.service.impl;

import korastudy.be.dto.response.CertificateDetailDTO;
import korastudy.be.dto.response.course.CourseProgressDTO;
import korastudy.be.dto.response.course.CourseProgressDetailDTO;
import korastudy.be.dto.response.CertificateDTO;
import korastudy.be.entity.*;
import korastudy.be.entity.Course.*;
import korastudy.be.entity.Enum.CertificateGrade;
import korastudy.be.entity.Enum.EnrollmentStatus;
import korastudy.be.entity.Enum.ProgressStatus;
import korastudy.be.entity.User.User;
import korastudy.be.repository.*;
import korastudy.be.service.IQuizService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseCompletionService {

    private final EnrollmentRepository enrollmentRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final CertificateRepository certificateRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final QuizRepository quizRepository;
    private final IQuizService quizService;
    private final UserRepository userRepository;

    // ==================== TIẾN ĐỘ VÀ HOÀN THÀNH ====================

    @Transactional
    public void updateCourseProgress(Long userId, Long courseId) {
        log.info("Updating course progress for userId: {}, courseId: {}", userId, courseId);

        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        //  Lưu trạng thái CŨ
        EnrollmentStatus oldStatus = enrollment.getStatus();

        // Nếu đã hoàn thành từ trước
        if (oldStatus == EnrollmentStatus.COMPLETED) {
            log.info("Course already completed, checking for certificate update...");
            //  CHỈ update điểm nếu cần, KHÔNG tạo cert mới
            updateCertificateScoreIfNeeded(userId, courseId);
            return;
        }

        // Tính toán tiến độ mới
        double progress = calculateCourseProgress(userId, courseId);
        enrollment.setProgress(progress);
        enrollment.setCompletedLessons(getCompletedLessonsCount(userId, courseId));

        // Kiểm tra nếu VỪA MỚI hoàn thành
        boolean isCompleted = isCourseCompleted(userId, courseId);
        log.info("Course completion check - userId: {}, courseId: {}, isCompleted: {}, oldStatus: {}",
                userId, courseId, isCompleted, oldStatus);

        if (isCompleted && oldStatus != EnrollmentStatus.COMPLETED) {
            // certificate khi VỪA MỚI chuyển sang COMPLETED
            log.info("🎉 User {} just completed course {}, creating certificate...",
                    userId, courseId);
            completeCourse(enrollment);
        } else {
            enrollmentRepository.save(enrollment);
        }
    }


    /**
     * Chỉ update certificate khi điểm quiz TĂNG
     */
    private void updateCertificateScoreIfNeeded(Long userId, Long courseId) {
        Optional<Certificate> certificateOpt = getUserCertificateSafe(userId, courseId);

        if (certificateOpt.isEmpty()) {
            log.debug("No certificate found for user {} course {}, skipping update",
                    userId, courseId);
            return;
        }

        Certificate certificate = certificateOpt.get();
        Double currentScore = certificate.getAverageScore();
        Double newScore = calculateUserAverageScoreInCourse(userId, courseId);

        //  CHỈ update khi điểm MỚI cao HƠN điểm CŨ
        if (newScore != null && (currentScore == null || newScore > currentScore)) {
            log.info("📈 Updating certificate score for user {} course {}: {} → {}",
                    userId, courseId, currentScore, newScore);

            updateCertificateScoreIfHigher(certificate, userId, courseId);
        } else {
            log.debug("Score not improved for user {} course {}: current={}, new={}",
                    userId, courseId, currentScore, newScore);
        }
    }


    /**
     * Lấy certificate của user (an toàn với duplicate)
     */
    private Optional<Certificate> getUserCertificateSafe(Long userId, Long courseId) {
        List<Certificate> certificates = certificateRepository.findByUserIdAndCourseId(userId, courseId);

        if (certificates.isEmpty()) {
            return Optional.empty();
        }

        if (certificates.size() > 1) {
            log.error("🚨 CRITICAL: Found {} duplicate certificates for user {} and course {}. " + "Certificate IDs: {}. THIS SHOULD NOT HAPPEN!", certificates.size(), userId, courseId, certificates.stream().map(c -> String.format("ID=%d, Code=%s, Date=%s", c.getId(), c.getCertificateCode(), c.getCertificateDate())).collect(Collectors.joining(", ")));
        }

        return certificates.stream().max(Comparator.comparing(cert -> cert.getCertificateDate() != null ? cert.getCertificateDate().atStartOfDay() : LocalDateTime.MIN));
    }

    public boolean isCourseCompleted(Long userId, Long courseId) {
        List<Lesson> allLessons = lessonRepository.findAllByCourseId(courseId);
        boolean allLessonsCompleted = allLessons.stream().allMatch(lesson -> {
            Optional<LessonProgress> progress = lessonProgressRepository.findByUserIdAndLessonId(userId, lesson.getId());
            return progress.isPresent() && progress.get().getStatus() == ProgressStatus.COMPLETED;
        });

        if (!allLessonsCompleted) {
            return false;
        }

        List<Quiz> allQuizzes = quizRepository.findPublishedByCourseId(courseId);
        return allQuizzes.stream().allMatch(quiz -> isQuizPassed(userId, quiz.getId()));
    }

    @Transactional
    public void completeCourse(Enrollment enrollment) {
        Long userId = enrollment.getUser().getId();
        Long courseId = enrollment.getCourse().getId();

        log.info("Completing course for userId: {}, courseId: {}", userId, courseId);

        enrollment.markAsCompleted();
        enrollmentRepository.save(enrollment);

        createCertificateIfEligible(userId, courseId);
    }

    /**
     * Tạo certificate với protection tốt hơn
     */
    @Transactional
    public Certificate createCertificateIfEligible(Long userId, Long courseId) {

        Optional<Certificate> existing = getUserCertificateSafe(userId, courseId);
        if (existing.isPresent()) {
            return existing.get();
        }

        if (!isCourseCompleted(userId, courseId)) {
            throw new IllegalStateException("User has not completed the course");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        try {
            Certificate certificate = createNewCertificate(user, course, userId);
            certificateRepository.flush(); // 🔥 then catch works
            return certificate;

        } catch (DataIntegrityViolationException e) {
            return getUserCertificateSafe(userId, courseId)
                    .orElseThrow(() -> new RuntimeException("Certificate exists but cannot retrieve"));
        }
    }

    private Certificate updateCertificateScoreIfHigher(Certificate existingCertificate, Long userId, Long courseId) {
        Double currentAverageScore = existingCertificate.getAverageScore();
        Double newAverageScore = calculateUserAverageScoreInCourse(userId, courseId);

        if (newAverageScore != null && (currentAverageScore == null || newAverageScore > currentAverageScore)) {
            log.info("Updating certificate score from {} to {}", currentAverageScore, newAverageScore);

            existingCertificate.setAverageScore(newAverageScore);
            CertificateGrade newGrade = CertificateGrade.fromScore(newAverageScore);
            existingCertificate.setGrade(newGrade.name());
            existingCertificate.setCertificateName(generateCertificateName(existingCertificate.getCourse(), newGrade, newAverageScore));

            return certificateRepository.save(existingCertificate);
        }

        return existingCertificate;
    }

    /**
     * Lấy certificate bằng certificateCode
     */
    public Optional<Certificate> getCertificateByCode(String certificateCode) {
        return certificateRepository.findByCertificateCode(certificateCode);
    }

    /**
     * Check tồn tại certificate bằng code
     */
    public boolean existsByCertificateCode(String certificateCode) {
        return certificateRepository.existsByCertificateCode(certificateCode);
    }

    public Optional<Certificate> getCertificateById(Long certificateId) {
        return certificateRepository.findById(certificateId);
    }

    /**
     * Lấy chi tiết certificate bằng code
     */
    public CertificateDetailDTO getCertificateDetailByCode(String certificateCode) {
        Certificate certificate = certificateRepository.findByCertificateCode(certificateCode)
                .orElseThrow(() -> new RuntimeException("Certificate not found"));

        return getCertificateDetail(certificate.getId());
    }

    public CertificateDetailDTO getCertificateDetail(Long certificateId) {
        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new RuntimeException("Certificate not found"));

        return CertificateDetailDTO.builder()
                .id(certificate.getId())
                .certificateCode(certificate.getCertificateCode())
                .certificateName(certificate.getCertificateName())
                .certificateDate(certificate.getCertificateDate())
                .grade(certificate.getGrade())
                .averageScore(certificate.getAverageScore())
                .userName(certificate.getUser().getFullName())
                .userEmail(certificate.getUser().getAccount().getEmail())
                .courseName(certificate.getCourse().getCourseName())
                .courseDescription(certificate.getCourse().getCourseDescription())
                .createdAt(certificate.getCreatedAt())
                .updatedAt(certificate.getLastModified())
                .build();
    }

    // ==================== TẠO CERTIFICATE ====================

    private Certificate createNewCertificate(User user, Course course, Long userId) {
        Double averageScore = calculateUserAverageScoreInCourse(userId, course.getId());
        log.info("Calculated average score for user: {}, course: {}, score: {}", userId, course.getId(), averageScore);

        CertificateGrade grade = CertificateGrade.fromScore(averageScore != null ? averageScore : 0.0);
        String certificateCode = generateCertificateCode(userId, course.getId());

        Certificate certificate = Certificate.builder().certificateCode(certificateCode).certificateName(generateCertificateName(course, grade, averageScore)).certificateDate(LocalDate.now()).user(user).course(course).grade(grade.name()).averageScore(averageScore).build();

        Certificate savedCertificate = certificateRepository.save(certificate);
        log.info("New certificate created - id: {}, code: {}, grade: {}, score: {}", savedCertificate.getId(), certificateCode, grade, averageScore);

        return savedCertificate;
    }

    private String generateCertificateCode(Long userId, Long courseId) {
        return "CERT-" + System.currentTimeMillis() + "-" + userId + "-" + courseId;
    }

    // ==================== KIỂM TRA QUIZ ====================

    private boolean isQuizPassed(Long userId, Long quizId) {
        try {
            var quizStatus = quizService.getQuizStatusForStudent(quizId, userId);

            if (quizStatus.getIsCompleted() == null || !quizStatus.getIsCompleted()) {
                return false;
            }

            Double bestScore = quizStatus.getBestScore();
            if (bestScore == null || bestScore == 0) {
                return false;
            }

            Optional<Quiz> quizOpt = quizRepository.findById(quizId);
            if (quizOpt.isEmpty()) {
                return false;
            }

            Quiz quiz = quizOpt.get();
            return bestScore >= quiz.getPassingScore();

        } catch (Exception e) {
            log.warn("Error checking quiz status for user {} quiz {}: {}", userId, quizId, e.getMessage());
            return false;
        }
    }

    private Double getQuizBestScore(Long userId, Long quizId) {
        try {
            var quizStatus = quizService.getQuizStatusForStudent(quizId, userId);
            return quizStatus.getBestScore() != null ? quizStatus.getBestScore() : 0.0;
        } catch (Exception e) {
            log.warn("Error getting quiz best score: {}", e.getMessage());
            return 0.0;
        }
    }


    // ==================== TÍNH TOÁN ====================

    private double calculateCourseProgress(Long userId, Long courseId) {
        List<Lesson> allLessons = lessonRepository.findAllByCourseId(courseId);
        List<Quiz> allQuizzes = quizRepository.findPublishedByCourseId(courseId);

        if (allLessons.isEmpty() && allQuizzes.isEmpty()) {
            return 0.0;
        }

        int totalItems = allLessons.size() + allQuizzes.size();
        int completedItems = 0;

        for (Lesson lesson : allLessons) {
            Optional<LessonProgress> progress = lessonProgressRepository.findByUserIdAndLessonId(userId, lesson.getId());
            if (progress.isPresent() && progress.get().getStatus() == ProgressStatus.COMPLETED) {
                completedItems++;
            }
        }

        for (Quiz quiz : allQuizzes) {
            if (isQuizPassed(userId, quiz.getId())) {
                completedItems++;
            }
        }

        return (double) completedItems / totalItems * 100;
    }

    private Integer getCompletedLessonsCount(Long userId, Long courseId) {
        List<Lesson> allLessons = lessonRepository.findAllByCourseId(courseId);
        return (int) allLessons.stream().filter(lesson -> {
            Optional<LessonProgress> progress = lessonProgressRepository.findByUserIdAndLessonId(userId, lesson.getId());
            return progress.isPresent() && progress.get().getStatus() == ProgressStatus.COMPLETED;
        }).count();
    }

    private Double calculateUserAverageScoreInCourse(Long userId, Long courseId) {
        try {
            List<Quiz> allQuizzes = quizRepository.findPublishedByCourseId(courseId);
            if (allQuizzes.isEmpty()) {
                return 100.0;
            }

            double totalScore = 0;
            int quizCount = 0;

            for (Quiz quiz : allQuizzes) {
                Double bestScore = getQuizBestScore(userId, quiz.getId());
                if (bestScore != null && bestScore > 0) {
                    totalScore += bestScore;
                    quizCount++;
                }
            }

            return quizCount > 0 ? totalScore / quizCount : 0.0;

        } catch (Exception e) {
            log.warn("Could not calculate average score: {}", e.getMessage());
            return 0.0;
        }
    }

    // ==================== DTO & RESPONSE ====================

    /**
     * ⭐ FIX: Dùng getUserCertificateSafe thay vì findByUserIdAndCourseId
     */
    public CourseProgressDetailDTO getCourseProgressDetail(Long userId, Long courseId) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new RuntimeException("Course not found"));

        List<Lesson> allLessons = lessonRepository.findAllByCourseId(courseId);
        int totalLessons = allLessons.size();
        int completedLessons = getCompletedLessonsCount(userId, courseId);
        double lessonCompletionRate = totalLessons > 0 ? (double) completedLessons / totalLessons * 100 : 0;

        List<Quiz> allQuizzes = quizRepository.findPublishedByCourseId(courseId);
        int totalQuizzes = allQuizzes.size();
        int passedQuizzes = 0;
        double totalQuizScore = 0.0;

        for (Quiz quiz : allQuizzes) {
            if (isQuizPassed(userId, quiz.getId())) {
                passedQuizzes++;
                Double bestScore = getQuizBestScore(userId, quiz.getId());
                totalQuizScore += bestScore != null ? bestScore : 0;
            }
        }

        double quizPassRate = totalQuizzes > 0 ? (double) passedQuizzes / totalQuizzes * 100 : 0;
        double averageQuizScore = passedQuizzes > 0 ? totalQuizScore / passedQuizzes : 0;

        boolean isCompleted = isCourseCompleted(userId, courseId);
        boolean hasCert = hasCertificate(userId, courseId);

        String certificateGrade = null;
        String certificateMessage = null;

        // ⭐ FIX: Dùng getUserCertificateSafe
        Optional<Certificate> certificateOpt = getUserCertificateSafe(userId, courseId);
        if (certificateOpt.isPresent()) {
            Certificate certificate = certificateOpt.get();
            certificateGrade = certificate.getGrade();
            certificateMessage = generateCertificateMessage(certificate.getGrade());
        }

        return CourseProgressDetailDTO.builder().courseId(courseId).courseName(course.getCourseName()).totalLessons(totalLessons).completedLessons(completedLessons).lessonCompletionRate(Math.round(lessonCompletionRate * 100.0) / 100.0).totalQuizzes(totalQuizzes).passedQuizzes(passedQuizzes).quizPassRate(Math.round(quizPassRate * 100.0) / 100.0).averageQuizScore(Math.round(averageQuizScore * 100.0) / 100.0).isCompleted(isCompleted).motivationalMessage(generateMotivationalMessage(lessonCompletionRate, quizPassRate, averageQuizScore, isCompleted)).recommendations(generateRecommendations(lessonCompletionRate, quizPassRate, averageQuizScore, isCompleted)).certificateGrade(certificateGrade).certificateMessage(certificateMessage).hasCertificate(hasCert).build();
    }

    public CourseProgressDTO getCourseProgressSummary(Long userId, Long courseId) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new RuntimeException("Course not found"));

        List<Lesson> allLessons = lessonRepository.findAllByCourseId(courseId);
        List<Quiz> allQuizzes = quizRepository.findPublishedByCourseId(courseId);

        int totalItems = allLessons.size() + allQuizzes.size();
        int completedLessons = 0;
        int passedQuizzes = 0;

        for (Lesson lesson : allLessons) {
            Optional<LessonProgress> progress = lessonProgressRepository.findByUserIdAndLessonId(userId, lesson.getId());
            if (progress.isPresent() && progress.get().getStatus() == ProgressStatus.COMPLETED) {
                completedLessons++;
            }
        }

        for (Quiz quiz : allQuizzes) {
            if (isQuizPassed(userId, quiz.getId())) {
                passedQuizzes++;
            }
        }

        int completedItems = completedLessons + passedQuizzes;
        double progressPercentage = totalItems > 0 ? (double) completedItems / totalItems * 100 : 0;

        boolean isCompleted = isCourseCompleted(userId, courseId);
        boolean hasCert = hasCertificate(userId, courseId);

        return CourseProgressDTO.builder().courseId(courseId).courseName(course.getCourseName()).totalLessons(allLessons.size()).completedLessons(completedLessons).totalQuizzes(allQuizzes.size()).passedQuizzes(passedQuizzes).progressPercentage(progressPercentage).isCompleted(isCompleted).hasCertificate(hasCert).build();
    }

    public CertificateDTO convertToDTO(Certificate certificate) {
        return CertificateDTO.builder()
                .id(certificate.getId())
                .certificateCode(certificate.getCertificateCode())
                .certificateName(certificate.getCertificateName())
                .certificateDate(certificate.getCertificateDate())
                .grade(certificate.getGrade())
                .averageScore(certificate.getAverageScore())
                .userId(certificate.getUser() != null ? certificate.getUser().getId() : null)
                .courseId(certificate.getCourse() != null ? certificate.getCourse().getId() : null)
                .courseName(certificate.getCourse() != null ? certificate.getCourse().getCourseName() : null)
                .detailUrl("/api/v1/certificates/" + certificate.getId())  // URL bằng ID
                .shareUrl("/certificates/code/" + certificate.getCertificateCode())  // URL bằng code
                .verifyUrl("/api/v1/certificates/public/verify/" + certificate.getCertificateCode())
                .build();
    }

    public List<CertificateDTO> getUserCertificateDTOs(Long userId) {
        List<Certificate> certificates = certificateRepository.findByUserId(userId);
        return certificates.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // ==================== UTILITY METHODS ====================

    private String generateCertificateName(Course course, CertificateGrade grade, Double averageScore) {
        StringBuilder name = new StringBuilder();

        switch (grade) {
            case EXCELLENT:
                name.append("Chứng chỉ Xuất sắc - ");
                break;
            case GOOD:
                name.append("Chứng chỉ Giỏi - ");
                break;
            case FAIR:
                name.append("Chứng chỉ Khá - ");
                break;
            default:
                name.append("Chứng chỉ Hoàn thành - ");
                break;
        }

        name.append(course.getCourseName());

        if (averageScore != null && averageScore > 0) {
            name.append(" (Điểm: ").append(String.format("%.1f", averageScore)).append(")");
        }

        return name.toString();
    }

    private String generateMotivationalMessage(double lessonRate, double quizRate, double avgScore, boolean isCompleted) {
        if (isCompleted) {
            if (avgScore >= 90) {
                return "🎉 Xuất sắc! Bạn đã hoàn thành khóa học với kết quả tuyệt vời!";
            } else if (avgScore >= 80) {
                return "👍 Giỏi lắm! Bạn đã hoàn thành khóa học thành công!";
            } else if (avgScore >= 70) {
                return "💪 Khá tốt! Bạn đã hoàn thành khóa học!";
            } else {
                return "✅ Chúc mừng! Bạn đã hoàn thành khóa học!";
            }
        } else if (lessonRate == 100 && quizRate == 100) {
            return "✨ Tuyệt vời! Bạn đã hoàn thành tất cả bài học và quiz!";
        } else if (lessonRate >= 90 && quizRate >= 90) {
            return "🌟 Bạn đang rất gần với việc hoàn thành khóa học!";
        } else if (lessonRate >= 80 || quizRate >= 80) {
            return "🚀 Tiến độ rất tốt! Hãy tiếp tục phát huy!";
        } else if (lessonRate >= 50 || quizRate >= 50) {
            return "📚 Bạn đã đi được nửa chặng đường! Cố gắng thêm chút nữa!";
        } else {
            return "🌱 Hãy bắt đầu từng bước nhỏ! Mỗi bài học đều có giá trị!";
        }
    }

    /**
     * Tạo gợi ý cải thiện
     */
    private List<String> generateRecommendations(double lessonRate, double quizRate, double avgScore, boolean isCompleted) {
        List<String> recommendations = new ArrayList<>();

        if (!isCompleted) {
            if (lessonRate < 100) {
                recommendations.add("Hoàn thành các bài học còn lại (" + (100 - (int) lessonRate) + "% cần hoàn thành)");
            }

            if (quizRate < 100) {
                recommendations.add("Làm lại các bài quiz chưa đạt (" + (100 - (int) quizRate) + "% cần hoàn thành)");
            }
        }

        if (lessonRate == 100 && quizRate == 100 && avgScore < 90 && !isCompleted) {
            recommendations.add("Ôn tập lại để cải thiện điểm số trung bình");
        }

        if (avgScore < 70 && quizRate > 0) {
            recommendations.add("Xem lại các câu trả lời sai trong quiz để hiểu sâu hơn");
        }

        if (recommendations.isEmpty() && isCompleted) {
            recommendations.add("Khám phá các khóa học liên quan để nâng cao kiến thức");
        }

        return recommendations;
    }

    /**
     * Tạo thông báo cho certificate
     */
    private String generateCertificateMessage(String grade) {
        if (grade == null) return null;

        try {
            CertificateGrade certificateGrade = CertificateGrade.valueOf(grade);
            return switch (certificateGrade) {
                case EXCELLENT -> "Xuất sắc! Bạn đã thể hiện sự hiểu biết sâu sắc về nội dung khóa học.";
                case GOOD -> "Giỏi! Bạn đã nắm vững kiến thức trọng tâm của khóa học.";
                case FAIR -> "Khá! Bạn đã hoàn thành khóa học với kết quả tốt.";
                default -> "Chúc mừng! Bạn đã hoàn thành khóa học thành công.";
            };
        } catch (Exception e) {
            return "Chúc mừng! Bạn đã hoàn thành khóa học.";
        }
    }

    /**
     * Kiểm tra xem user đã có certificate chưa
     */
    public boolean hasCertificate(Long userId, Long courseId) {
        return certificateRepository.existsByUserIdAndCourseId(userId, courseId);
    }

    /**
     * Lấy thông tin certificate của user
     */
    public Optional<Certificate> getUserCertificate(Long userId, Long courseId) {
        return getUserCertificateSafe(userId, courseId);
    }

    /**
     * Lấy điểm trung bình của user trong khóa học
     */
    public Double getUserAverageScore(Long userId, Long courseId) {
        return calculateUserAverageScoreInCourse(userId, courseId);
    }

    /**
     * Lấy tất cả certificate của user (entity)
     */
    public List<Certificate> getUserCertificates(Long userId) {
        return certificateRepository.findByUserId(userId);
    }

    /**
     * Kiểm tra user có đủ điều kiện nhận certificate không
     */
    public boolean isEligibleForCertificate(Long userId, Long courseId) {
        return isCourseCompleted(userId, courseId);
    }

    /**
     * Lấy thống kê certificate của user
     */
    public Map<String, Object> getCertificateStats(Long userId) {
        List<Certificate> certificates = certificateRepository.findByUserId(userId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCertificates", certificates.size());

        long excellentCount = certificates.stream().filter(c -> "EXCELLENT".equals(c.getGrade())).count();
        long goodCount = certificates.stream().filter(c -> "GOOD".equals(c.getGrade())).count();
        long fairCount = certificates.stream().filter(c -> "FAIR".equals(c.getGrade())).count();
        long passCount = certificates.stream().filter(c -> "PASS".equals(c.getGrade()) || c.getGrade() == null).count();

        stats.put("excellentCount", excellentCount);
        stats.put("goodCount", goodCount);
        stats.put("fairCount", fairCount);
        stats.put("passCount", passCount);

        double totalScore = certificates.stream().filter(c -> c.getAverageScore() != null).mapToDouble(Certificate::getAverageScore).sum();
        double averageScore = certificates.isEmpty() ? 0 : totalScore / certificates.size();

        stats.put("averageScore", Math.round(averageScore * 100.0) / 100.0);

        return stats;
    }

}