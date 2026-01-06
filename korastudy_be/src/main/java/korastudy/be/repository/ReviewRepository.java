package korastudy.be.repository;

import korastudy.be.entity.Enum.ReviewStatus;
import korastudy.be.entity.Enum.ReviewType;
import korastudy.be.entity.Review.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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

    @Query("SELECT COUNT(r) FROM Review r WHERE r.mockTest.id = :mockTestId AND r.status = :status")
    long countByMockTestIdAndStatus(@Param("mockTestId") Long mockTestId, @Param("status") ReviewStatus status);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.mockTest.id = :mockTestId AND r.status = 'ACTIVE'")
    Double findAverageRatingByMockTestId(@Param("mockTestId") Long mockTestId);

    // News reviews
    List<Review> findByNewsArticleIdAndStatus(Long newsArticleId, ReviewStatus status);

    Page<Review> findByNewsArticleIdAndStatus(Long newsArticleId, ReviewStatus status, Pageable pageable);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.newsArticle.id = :newsArticleId AND r.status = :status")
    long countByNewsArticleIdAndStatus(@Param("newsArticleId") Long newsArticleId, @Param("status") ReviewStatus status);

    Optional<Review> findByUserIdAndNewsArticleId(Long userId, Long newsArticleId);

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
            "(r.reviewType = 'MOCK_TEST' AND r.mockTest.id = :targetId) OR " +
            "(r.reviewType = 'NEWS' AND r.newsArticle.id = :targetId))")
    Optional<Review> findByUserIdAndTargetId(
            @Param("userId") Long userId,
            @Param("targetId") Long targetId);

    // Đếm theo status
    long countByStatus(ReviewStatus status);

    // Đếm theo review type
    long countByReviewType(ReviewType reviewType);

    // Rating trung bình tất cả reviews
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.status = 'ACTIVE'")
    Double findOverallAverageRating();

    // ========== STATISTICS & DASHBOARD QUERIES ==========

    // Đếm reviews theo course và rating cụ thể
    @Query("SELECT COUNT(r) FROM Review r WHERE r.course.id = :courseId AND r.rating = :rating")
    long countByCourseIdAndRating(@Param("courseId") Long courseId, @Param("rating") Integer rating);

    // Đếm reviews theo mock test và rating cụ thể
    @Query("SELECT COUNT(r) FROM Review r WHERE r.mockTest.id = :mockTestId AND r.rating = :rating")
    long countByMockTestIdAndRating(@Param("mockTestId") Long mockTestId, @Param("rating") Integer rating);

    // Đếm reviews theo rating (1-5 sao)
    @Query("SELECT COUNT(r) FROM Review r WHERE r.rating = :rating")
    long countByRating(@Param("rating") Integer rating);

    // Average rating by type
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.reviewType = :reviewType AND r.status = 'ACTIVE'")
    Double findAverageRatingByType(@Param("reviewType") ReviewType reviewType);

    // Count reviews created after a specific date
    @Query("SELECT COUNT(r) FROM Review r WHERE r.createdAt >= :date")
    long countByCreatedAtAfter(@Param("date") LocalDateTime date);

    // Count course reviews created after a specific date
    @Query("SELECT COUNT(r) FROM Review r WHERE r.course.id = :courseId AND r.createdAt >= :date")
    long countByCourseIdAndCreatedAtAfter(@Param("courseId") Long courseId, @Param("date") LocalDateTime date);

    // Count mock test reviews created after a specific date
    @Query("SELECT COUNT(r) FROM Review r WHERE r.mockTest.id = :mockTestId AND r.createdAt >= :date")
    long countByMockTestIdAndCreatedAtAfter(@Param("mockTestId") Long mockTestId, @Param("date") LocalDateTime date);

    // Count reviews between two dates
    @Query("SELECT COUNT(r) FROM Review r WHERE r.createdAt BETWEEN :startDate AND :endDate")
    long countByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Average rating in date range
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.createdAt BETWEEN :startDate AND :endDate AND r.status = 'ACTIVE'")
    Double findAverageRatingByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Top-rated courses (with minimum 3 reviews)
    @Query("SELECT c.id, c.courseName, AVG(r.rating) as avgRating, COUNT(r) as reviewCount " +
            "FROM Review r JOIN r.course c " +
            "WHERE r.reviewType = 'COURSE' AND r.status = 'ACTIVE' " +
            "GROUP BY c.id, c.courseName " +
            "HAVING COUNT(r) >= 3 " +
            "ORDER BY avgRating DESC")
    List<Object[]> findTopRatedCourses(@Param("limit") int limit);

    // Top-rated mock tests (with minimum 2 reviews)
    @Query("SELECT m.id, m.title, AVG(r.rating) as avgRating, COUNT(r) as reviewCount " +
            "FROM Review r JOIN r.mockTest m " +
            "WHERE r.reviewType = 'MOCK_TEST' AND r.status = 'ACTIVE' " +
            "GROUP BY m.id, m.title " +
            "HAVING COUNT(r) >= 2 " +
            "ORDER BY avgRating DESC")
    List<Object[]> findTopRatedMockTests(@Param("limit") int limit);

    // ========== PAGINATION QUERIES ==========

    // Find reviews by status with pagination
    Page<Review> findByStatus(ReviewStatus status, Pageable pageable);

    // Find reported reviews with pagination
    @Query("SELECT r FROM Review r WHERE r.status = 'REPORTED'")
    Page<Review> findReportedReviews(Pageable pageable);

    // Find hidden reviews with pagination
    @Query("SELECT r FROM Review r WHERE r.status = 'HIDDEN'")
    Page<Review> findHiddenReviews(Pageable pageable);

    // Find deleted reviews with pagination
    @Query("SELECT r FROM Review r WHERE r.status = 'DELETED'")
    Page<Review> findDeletedReviews(Pageable pageable);

    // ========== SEARCH QUERIES ==========

    // Search reviews by comment content
    @Query("SELECT r FROM Review r WHERE LOWER(r.comment) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Review> searchByComment(@Param("keyword") String keyword, Pageable pageable);

    // Search reviews by username
    @Query("SELECT r FROM Review r JOIN r.user u WHERE LOWER(u.account.username) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Review> searchByUsername(@Param("keyword") String keyword, Pageable pageable);

    // Search reviews by course name
    @Query("SELECT r FROM Review r JOIN r.course c WHERE LOWER(c.courseName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Review> searchByCourseName(@Param("keyword") String keyword, Pageable pageable);

    // Search reviews by mock test title
    @Query("SELECT r FROM Review r JOIN r.mockTest m WHERE LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Review> searchByMockTestTitle(@Param("keyword") String keyword, Pageable pageable);


    @Query("SELECT COUNT(r) FROM Review r WHERE r.user.id = :userId AND r.status = :status")
    long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") ReviewStatus status);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);


    @Query(value = "SELECT r.course.id, r.course.courseName, COUNT(r) as reviewCount, AVG(r.rating) as avgRating " +
            "FROM Review r " +
            "WHERE r.reviewType = 'COURSE' " +
            "AND r.course IS NOT NULL " +
            "AND r.status = 'ACTIVE' " +
            "GROUP BY r.course.id, r.course.courseName " +
            "ORDER BY COUNT(r) DESC")
    List<Object[]> findTopReviewedCourses(@Param("limit") int limit);

    Long countByCreatedAtBetweenAndStatus(LocalDateTime startOfDay, LocalDateTime endOfDay, ReviewStatus reviewStatus);
}