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

    // Đếm số người dùng unique đã làm bài thi
    @Query("SELECT COUNT(DISTINCT c.user.id) FROM ComprehensiveTestResult c WHERE c.mockTest.id = :mockTestId")
    Long countDistinctUsersByMockTestId(@Param("mockTestId") Long mockTestId);
}
