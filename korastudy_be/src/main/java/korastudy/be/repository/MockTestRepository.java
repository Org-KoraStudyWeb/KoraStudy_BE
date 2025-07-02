package korastudy.be.repository;

import korastudy.be.entity.MockTest.MockTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MockTestRepository extends JpaRepository<MockTest, Long> {
    
    /**
     * Find all mock tests with pagination
     */
    Page<MockTest> findAll(Pageable pageable);
    
    /**
     * Find mock tests by level
     */
    Page<MockTest> findByLevelContainingIgnoreCase(String level, Pageable pageable);
    
    /**
     * Find mock tests by title containing search term
     */
    Page<MockTest> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    
    /**
     * Find mock tests by level and title containing search term
     */
    Page<MockTest> findByLevelContainingIgnoreCaseAndTitleContainingIgnoreCase(
            String level, String title, Pageable pageable);
    
    /**
     * Find mock test by ID with parts and questions
     */
    @Query("SELECT mt FROM MockTest mt " +
           "LEFT JOIN FETCH mt.parts p " +
           "LEFT JOIN FETCH p.questions " +
           "WHERE mt.id = :id")
    Optional<MockTest> findByIdWithPartsAndQuestions(@Param("id") Long id);
    
    /**
     * Find popular mock tests based on test results count
     */
    @Query("SELECT mt FROM MockTest mt " +
           "LEFT JOIN mt.practiceTestResults tr " +
           "GROUP BY mt.id " +
           "ORDER BY COUNT(tr.id) DESC")
    List<MockTest> findPopularMockTests(Pageable pageable);
    
    /**
     * Search mock tests by multiple criteria
     */
    @Query("SELECT mt FROM MockTest mt WHERE " +
           "(:title IS NULL OR LOWER(mt.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
           "(:level IS NULL OR LOWER(mt.level) LIKE LOWER(CONCAT('%', :level, '%'))) AND " +
           "(:minQuestions IS NULL OR mt.totalQuestions >= :minQuestions) AND " +
           "(:maxQuestions IS NULL OR mt.totalQuestions <= :maxQuestions)")
    Page<MockTest> searchMockTests(
            @Param("title") String title,
            @Param("level") String level,
            @Param("minQuestions") Integer minQuestions,
            @Param("maxQuestions") Integer maxQuestions,
            Pageable pageable);
    
    /**
     * Find mock tests by difficulty level
     */
    @Query("SELECT mt FROM MockTest mt WHERE " +
           "(:difficulty = 'EASY' AND mt.totalQuestions <= 30) OR " +
           "(:difficulty = 'MEDIUM' AND mt.totalQuestions > 30 AND mt.totalQuestions <= 60) OR " +
           "(:difficulty = 'HARD' AND mt.totalQuestions > 60)")
    Page<MockTest> findByDifficulty(@Param("difficulty") String difficulty, Pageable pageable);
    
    /**
     * Count total mock tests
     */
    long count();
    
    /**
     * Find active mock tests (assuming there's an active field)
     */
    @Query("SELECT mt FROM MockTest mt WHERE mt.id IS NOT NULL")
    Page<MockTest> findActiveMockTests(Pageable pageable);
}
