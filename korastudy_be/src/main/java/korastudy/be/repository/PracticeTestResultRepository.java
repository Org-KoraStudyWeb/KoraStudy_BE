package korastudy.be.repository;

import korastudy.be.entity.MockTest.PracticeTestResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PracticeTestResultRepository extends JpaRepository<PracticeTestResult, Long> {
    
    /**
     * Find results by user username
     */
    @Query("SELECT ptr FROM PracticeTestResult ptr " +
           "JOIN ptr.user u " +
           "WHERE u.account.username = :username " +
           "ORDER BY ptr.testDate DESC")
    Page<PracticeTestResult> findByUserUsername(@Param("username") String username, Pageable pageable);
    
    /**
     * Find results by user username and mock test ID
     */
    @Query("SELECT ptr FROM PracticeTestResult ptr " +
           "JOIN ptr.user u " +
           "WHERE u.account.username = :username " +
           "AND ptr.mockTest.id = :mockTestId " +
           "ORDER BY ptr.testDate DESC")
    List<PracticeTestResult> findByUserUsernameAndMockTestId(@Param("username") String username, 
                                                             @Param("mockTestId") Long mockTestId);
    
    /**
     * Find result by ID and user username
     */
    @Query("SELECT ptr FROM PracticeTestResult ptr " +
           "JOIN ptr.user u " +
           "WHERE ptr.id = :resultId " +
           "AND u.account.username = :username")
    Optional<PracticeTestResult> findByIdAndUserUsername(@Param("resultId") Long resultId, 
                                                         @Param("username") String username);
    
    /**
     * Find recent results by user username
     */
    @Query("SELECT ptr FROM PracticeTestResult ptr " +
           "JOIN ptr.user u " +
           "WHERE u.account.username = :username " +
           "ORDER BY ptr.testDate DESC")
    List<PracticeTestResult> findRecentByUserUsername(@Param("username") String username, Pageable pageable);
    
    /**
     * Get user's average score
     */
    @Query("SELECT AVG(CAST(ptr.noCorrect AS DOUBLE) / CAST(ptr.mockTest.totalQuestions AS DOUBLE) * 100) " +
           "FROM PracticeTestResult ptr " +
           "JOIN ptr.user u " +
           "WHERE u.account.username = :username")
    Optional<Double> getAverageScoreByUsername(@Param("username") String username);
    
    /**
     * Count total attempts by user
     */
    @Query("SELECT COUNT(ptr) FROM PracticeTestResult ptr " +
           "JOIN ptr.user u " +
           "WHERE u.account.username = :username")
    Long countByUserUsername(@Param("username") String username);
    
    /**
     * Get user's performance by level
     */
    @Query("SELECT ptr.mockTest.level, " +
           "AVG(CAST(ptr.noCorrect AS DOUBLE) / CAST(ptr.mockTest.totalQuestions AS DOUBLE) * 100), " +
           "COUNT(ptr) " +
           "FROM PracticeTestResult ptr " +
           "JOIN ptr.user u " +
           "WHERE u.account.username = :username " +
           "GROUP BY ptr.mockTest.level")
    List<Object[]> getPerformanceByLevelAndUsername(@Param("username") String username);
    
    /**
     * Get results by mock test ID
     */
    Page<PracticeTestResult> findByMockTestId(Long mockTestId, Pageable pageable);
    
    /**
     * Get global statistics
     */
    @Query("SELECT COUNT(ptr), " +
           "AVG(CAST(ptr.noCorrect AS DOUBLE) / CAST(ptr.mockTest.totalQuestions AS DOUBLE) * 100) " +
           "FROM PracticeTestResult ptr")
    Object[] getGlobalStats();
    
    /**
     * Get level distribution
     */
    @Query("SELECT ptr.mockTest.level, COUNT(ptr) " +
           "FROM PracticeTestResult ptr " +
           "GROUP BY ptr.mockTest.level")
    List<Object[]> getLevelDistribution();
    
    /**
     * Get most popular level
     */
    @Query("SELECT ptr.mockTest.level " +
           "FROM PracticeTestResult ptr " +
           "GROUP BY ptr.mockTest.level " +
           "ORDER BY COUNT(ptr) DESC")
    List<String> getMostPopularLevels(Pageable pageable);
    
    /**
     * Find results by date range
     */
    @Query("SELECT ptr FROM PracticeTestResult ptr " +
           "WHERE ptr.testDate BETWEEN :startDate AND :endDate " +
           "ORDER BY ptr.testDate DESC")
    List<PracticeTestResult> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                            @Param("endDate") LocalDateTime endDate);
}
