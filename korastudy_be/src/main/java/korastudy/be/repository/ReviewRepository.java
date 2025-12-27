package korastudy.be.repository;

import korastudy.be.entity.Enum.ReviewStatus;
import korastudy.be.entity.Enum.ReviewType;
import korastudy.be.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // ========== BASIC QUERIES ==========

    // Course reviews
    Optional<Review> findByUserIdAndCourseId(Long userId, Long courseId);

    List<Review> findByCourseIdAndStatus(Long courseId, ReviewStatus status);

    Page<Review> findByCourseIdAndStatus(Long courseId, ReviewStatus status, Pageable pageable);

    Long countByCourseIdAndStatus(Long courseId, ReviewStatus status);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.course.id = :courseId AND r.status = 'ACTIVE'")
    Double findAverageRatingByCourseId(@Param("courseId") Long courseId);

    // MockTest reviews
    Optional<Review> findByUserIdAndMockTestId(Long userId, Long mockTestId);

    List<Review> findByMockTestIdAndStatus(Long mockTestId, ReviewStatus status);

    Page<Review> findByMockTestIdAndStatus(Long mockTestId, ReviewStatus status, Pageable pageable);

    Long countByMockTestIdAndStatus(Long mockTestId, ReviewStatus status);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.mockTest.id = :mockTestId AND r.status = 'ACTIVE'")
    Double findAverageRatingByMockTestId(@Param("mockTestId") Long mockTestId);

    // User reviews

    List<Review> findByUserIdAndStatus(Long userId, ReviewStatus status);

    Page<Review> findByUserIdAndStatus(Long userId, ReviewStatus status, Pageable pageable);

    // ========== FILTER QUERIES (CHO ADMIN) ==========

    @Query("SELECT r FROM Review r WHERE " +
            "(:reviewType IS NULL OR r.reviewType = :reviewType) AND " +
            "(:status IS NULL OR r.status = :status) AND " +
            "(:userId IS NULL OR r.user.id = :userId)")
    Page<Review> findReviewsWithFilters(
            @Param("reviewType") ReviewType reviewType,
            @Param("status") ReviewStatus status,
            @Param("userId") Long userId,
            Pageable pageable);

    // ========== HELPER QUERIES ==========

    @Query("SELECT r FROM Review r WHERE " +
            "r.user.id = :userId AND " +
            "((r.reviewType = 'COURSE' AND r.course.id = :targetId) OR " +
            "(r.reviewType = 'MOCK_TEST' AND r.mockTest.id = :targetId))")
    Optional<Review> findByUserIdAndTargetId(
            @Param("userId") Long userId,
            @Param("targetId") Long targetId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE " +
            "((:reviewType = 'COURSE' AND r.course.id = :targetId) OR " +
            "(:reviewType = 'MOCK_TEST' AND r.mockTest.id = :targetId)) AND " +
            "r.status = 'ACTIVE'")
    Double findAverageRatingByTarget(
            @Param("reviewType") ReviewType reviewType,
            @Param("targetId") Long targetId);

    @Query("SELECT COUNT(r) FROM Review r WHERE " +
            "((:reviewType = 'COURSE' AND r.course.id = :targetId) OR " +
            "(:reviewType = 'MOCK_TEST' AND r.mockTest.id = :targetId)) AND " +
            "r.status = :status")
    Long countByTargetAndStatus(
            @Param("reviewType") ReviewType reviewType,
            @Param("targetId") Long targetId,
            @Param("status") ReviewStatus status);
}