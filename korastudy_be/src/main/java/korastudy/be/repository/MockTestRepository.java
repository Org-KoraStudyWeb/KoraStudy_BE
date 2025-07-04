package korastudy.be.repository;

import korastudy.be.entity.MockTest.MockTest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MockTestRepository extends JpaRepository<MockTest, Long> {

    List<MockTest> findByLevel(String level);
}
