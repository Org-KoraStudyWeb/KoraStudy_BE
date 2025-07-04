package korastudy.be.repository;

import korastudy.be.entity.MockTest.ComprehensiveTestResult;
import korastudy.be.entity.User.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComprehensiveTestResultRepository extends JpaRepository<ComprehensiveTestResult, Long> {
    List<ComprehensiveTestResult> findByUserId(Long userId);

    List<ComprehensiveTestResult> findByUser(User user);
}
