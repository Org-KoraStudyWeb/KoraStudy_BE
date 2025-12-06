package korastudy.be.repository;

import korastudy.be.entity.Course.Enrollment;
import korastudy.be.entity.Enum.EnrollmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    
    List<Enrollment> findByUserId(Long userId);
    
    List<Enrollment> findByCourseId(Long courseId);
    
    Page<Enrollment> findByCourseId(Long courseId, Pageable pageable);
    
    int countByCourseId(Long courseId);
    
    Optional<Enrollment> findByUserIdAndCourseId(Long userId, Long courseId);
    
    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
    
    long count();

    List<Enrollment> findByUserIdAndStatus(Long userId, EnrollmentStatus status);

    @Query("SELECT e FROM Enrollment e WHERE e.user.id = :userId ORDER BY e.lastAccessed DESC")
    List<Enrollment> findByUserIdOrderByLastAccessedDesc(Long userId);

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.user.id = :userId AND e.status = 'ACTIVE'")
    Long countActiveEnrollmentsByUser(Long userId);

    @Query("SELECT e FROM Enrollment e WHERE e.user.id = :userId AND e.status = 'ACTIVE' ORDER BY e.enrollDate DESC")
    List<Enrollment> findActiveEnrollmentsByUser(Long userId);

    // 1. Tìm enrollment theo status (cho admin filter)
    List<Enrollment> findByStatus(EnrollmentStatus status);
    Page<Enrollment> findByStatus(EnrollmentStatus status, Pageable pageable);

    // 2. Thống kê theo course và status
    long countByCourseIdAndStatus(Long courseId, EnrollmentStatus status);

    // 3. Tìm enrollment sắp hết hạn (cho notification)
    @Query("SELECT e FROM Enrollment e WHERE e.expiryDate BETWEEN :startDate AND :endDate AND e.status = 'ACTIVE'")
    List<Enrollment> findExpiringEnrollments(LocalDate startDate, LocalDate endDate);

    // 4. Tìm enrollment đã hoàn thành (cho certificate)
    @Query("SELECT e FROM Enrollment e WHERE e.status = 'COMPLETED' AND e.progress >= 100")
    List<Enrollment> findCompletedEnrollments();

    // 5. Thống kê progress trung bình của khóa học
    @Query("SELECT AVG(e.progress) FROM Enrollment e WHERE e.course.id = :courseId AND e.status = 'ACTIVE'")
    Double findAverageProgressByCourseId(Long courseId);

    // 6. Tìm enrollment với course info (optimize query)
    @Query("SELECT e FROM Enrollment e JOIN FETCH e.course WHERE e.user.id = :userId")
    List<Enrollment> findByUserIdWithCourse(Long userId);

    // 7. Tìm enrollment với user info (cho admin)
    @Query("SELECT e FROM Enrollment e JOIN FETCH e.user WHERE e.course.id = :courseId")
    List<Enrollment> findByCourseIdWithUser(Long courseId);

    // 8. Kiểm tra enrollment có tồn tại và active
    default boolean existsActiveEnrollment(Long userId, Long courseId) {
        return existsByUserIdAndCourseIdAndStatus(userId, courseId, EnrollmentStatus.ACTIVE);
    }

    boolean existsByUserIdAndCourseIdAndStatus(Long userId, Long courseId, EnrollmentStatus status);

    long countByStatus(EnrollmentStatus status);
}
