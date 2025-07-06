package korastudy.be.repository;

import korastudy.be.entity.MockTest.PracticeTestResult;
import korastudy.be.entity.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PracticeTestResultRepository extends JpaRepository<PracticeTestResult, Long> {
    List<PracticeTestResult> findByUser(User user);
    List<PracticeTestResult> findByUserOrderByTestDateDesc(User user);
    List<PracticeTestResult> findByMockTest_IdAndUser(Long mockTestId, User user);
}
