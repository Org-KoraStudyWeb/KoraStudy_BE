package korastudy.be.repository;

import korastudy.be.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    // ❌ XÓA method này - nó không an toàn
    // Optional<Certificate> findByCourseId(Long courseId);

    //  Method chính - lấy 1 certificate duy nhất
    @Query("SELECT c FROM Certificate c " +
            "WHERE c.user.id = :userId AND c.course.id = :courseId " +
            "ORDER BY c.createdAt DESC")
    Optional<Certificate> findFirstByUserIdAndCourseId(
            @Param("userId") Long userId,
            @Param("courseId") Long courseId
    );

    // Lấy tất cả certificates (cho trường hợp cần xem lịch sử)
    List<Certificate> findByUserIdAndCourseId(Long userId, Long courseId);

    //  Check exists - an toàn và nhanh hơn
    boolean existsByUserIdAndCourseId(Long userId, Long courseId);

    //  Count để debug/monitoring
    @Query("SELECT COUNT(c) FROM Certificate c " +
            "WHERE c.user.id = :userId AND c.course.id = :courseId")
    long countByUserIdAndCourseId(
            @Param("userId") Long userId,
            @Param("courseId") Long courseId
    );

    // Lấy tất cả certificates của user
    @Query("SELECT c FROM Certificate c " +
            "WHERE c.user.id = :userId " +
            "ORDER BY c.createdAt DESC")
    List<Certificate> findByUserId(@Param("userId") Long userId);

    Optional<Certificate> findByCertificateCode(String certificateCode);
    boolean existsByCertificateCode(String certificateCode);
}