package korastudy.be.repository;

import korastudy.be.entity.MockTest.ComprehensiveTestResult;
import korastudy.be.entity.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ComprehensiveTestResultRepository extends JpaRepository<ComprehensiveTestResult, Long> {
    List<ComprehensiveTestResult> findByUserId(Long userId);

    List<ComprehensiveTestResult> findByUser(User user);
    
    // Tìm kiếm theo userId sắp xếp theo thời gian tạo giảm dần
    List<ComprehensiveTestResult> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Đếm số người dùng unique đã làm bài thi
    @Query("SELECT COUNT(DISTINCT c.user.id) FROM ComprehensiveTestResult c WHERE c.mockTest.id = :mockTestId")
    Long countDistinctUsersByMockTestId(@Param("mockTestId") Long mockTestId);

    // Dashboard stats queries
    // Đếm tổng số lượt thi
    long count();

    // Đếm số lượt thi hôm nay
    @Query("SELECT COUNT(c) FROM ComprehensiveTestResult c WHERE c.createdAt >= :startOfDay")
    long countByCreatedAtAfter(@Param("startOfDay") java.time.LocalDateTime startOfDay);

    // Đếm số lượt thi trong 7 ngày qua
    @Query("SELECT COUNT(c) FROM ComprehensiveTestResult c WHERE c.createdAt >= :startDate")
    long countTestsInPeriod(@Param("startDate") java.time.LocalDateTime startDate);

    // Tính điểm trung bình toàn hệ thống
    @Query("SELECT AVG(c.scores) FROM ComprehensiveTestResult c")
    Double getAverageScore();

    // Đếm số user unique đã làm bài
    @Query("SELECT COUNT(DISTINCT c.user.id) FROM ComprehensiveTestResult c")
    long countDistinctUsers();
}
