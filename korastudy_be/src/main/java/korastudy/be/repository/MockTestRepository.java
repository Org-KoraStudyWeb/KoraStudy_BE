package korastudy.be.repository;

import korastudy.be.entity.MockTest.MockTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MockTestRepository extends JpaRepository<MockTest, Long> {

    List<MockTest> findByLevel(String level);

    @Query("SELECT m FROM MockTest m WHERE " +
            "(:title IS NULL OR :title = '' OR LOWER(m.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            "(:level IS NULL OR :level = '' OR m.level = :level) AND " +
            "(:type IS NULL OR :type = '' OR m.level = :type)")
    Page<MockTest> searchMockTests(@Param("title") String title,
                                   @Param("level") String level,
                                   @Param("type") String type,
                                   Pageable pageable);
}
