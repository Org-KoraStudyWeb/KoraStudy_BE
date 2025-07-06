package korastudy.be.repository;

import korastudy.be.entity.MockTest.ExamComment;
import korastudy.be.entity.MockTest.MockTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamCommentRepository extends JpaRepository<ExamComment, Long> {
    List<ExamComment> findByMockTestOrderByCreatedAtDesc(MockTest mockTest);
    List<ExamComment> findByMockTest_IdOrderByCreatedAtDesc(Long mockTestId);
}
