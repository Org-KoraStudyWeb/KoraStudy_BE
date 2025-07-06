package korastudy.be.repository;

import korastudy.be.entity.MockTest.MockTestQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MockTestQuestionRepository extends JpaRepository<MockTestQuestion, Long> {
    List<MockTestQuestion> findByQuestionPart_Id(Long partId);

    @Query("SELECT COALESCE(MAX(q.questionOrder), 0) FROM MockTestQuestion q WHERE q.questionPart.id = :partId")
    Integer findMaxQuestionOrderByPartId(@Param("partId") Long partId);

    void deleteByQuestionPart_Id(Long partId);

    List<MockTestQuestion> findByQuestionPart_IdOrderByQuestionOrder(Long partId);

    @Query("SELECT COUNT(q) FROM MockTestQuestion q WHERE q.questionPart.mockTest.id = :mockTestId")
    Integer countByQuestionPart_MockTest_Id(@Param("mockTestId") Long mockTestId);
}
