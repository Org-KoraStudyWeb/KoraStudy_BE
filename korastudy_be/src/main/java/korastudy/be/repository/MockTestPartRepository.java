package korastudy.be.repository;

import korastudy.be.entity.MockTest.MockTestPart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MockTestPartRepository extends JpaRepository<MockTestPart, Long> {
    List<MockTestPart> findByMockTestId(Long mockTestId);

    @Query("SELECT COALESCE(MAX(p.partNumber), 0) FROM MockTestPart p WHERE p.mockTest.id = :mockTestId")
    Integer findMaxPartNumberByMockTestId(@Param("mockTestId") Long mockTestId);

    void deleteByMockTestId(Long mockTestId);

    List<MockTestPart> findByMockTestIdOrderByPartNumber(Long mockTestId);
}
