package korastudy.be.service;

import korastudy.be.dto.exam.MockTestDTO;
import korastudy.be.dto.exam.TestResultDTO;
import korastudy.be.dto.request.ExamSearchRequest;
import korastudy.be.dto.request.ExamSubmissionRequest;
import korastudy.be.dto.response.ExamListResponse;
import korastudy.be.dto.response.ExamResultResponse;
import korastudy.be.dto.response.ExamStatsResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IMockTestService {
    
    /**
     * Get all mock tests with pagination
     */
    ExamListResponse getAllMockTests(Pageable pageable);
    
    /**
     * Search mock tests with filters
     */
    ExamListResponse searchMockTests(ExamSearchRequest searchRequest);
    
    /**
     * Get mock test by ID with full details (questions and answers)
     */
    MockTestDTO getMockTestById(Long id);
    
    /**
     * Get mock test by ID for taking exam (without correct answers)
     */
    MockTestDTO getMockTestForExam(Long id);
    
    /**
     * Get popular mock tests
     */
    List<MockTestDTO> getPopularMockTests(int limit);
    
    /**
     * Submit exam answers and get result
     */
    ExamResultResponse submitExam(String username, ExamSubmissionRequest submission);
    
    /**
     * Get exam result by ID
     */
    ExamResultResponse getExamResult(Long resultId, String username);
    
    /**
     * Get user's exam history
     */
    List<TestResultDTO> getUserExamHistory(String username, Pageable pageable);
    
    /**
     * Get exam statistics for user
     */
    ExamStatsResponse getUserExamStats(String username);
    
    /**
     * Get exam statistics for admin dashboard
     */
    ExamStatsResponse getGlobalExamStats();
    
    /**
     * Get user's recent exam results
     */
    List<TestResultDTO> getRecentResults(String username, int limit);
    
    /**
     * Get exam results by mock test ID
     */
    List<TestResultDTO> getResultsByMockTestId(Long mockTestId, Pageable pageable);
}
