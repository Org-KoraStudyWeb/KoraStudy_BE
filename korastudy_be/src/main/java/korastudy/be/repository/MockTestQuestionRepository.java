package korastudy.be.repository;

import korastudy.be.entity.MockTest.MockTestQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MockTestQuestionRepository extends JpaRepository<MockTestQuestion, Long> {
    List<MockTestQuestion> findByQuestionPart_Id(Long partId);
}
