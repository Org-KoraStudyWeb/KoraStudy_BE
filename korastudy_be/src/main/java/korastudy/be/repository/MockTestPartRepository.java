package korastudy.be.repository;

import korastudy.be.entity.MockTest.MockTestPart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MockTestPartRepository extends JpaRepository<MockTestPart, Long> {
    List<MockTestPart> findByMockTestId(Long mockTestId);
}
