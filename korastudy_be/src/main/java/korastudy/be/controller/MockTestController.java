package korastudy.be.controller;

import jakarta.validation.Valid;
import korastudy.be.dto.exam.MockTestDTO;
import korastudy.be.dto.exam.TestResultDTO;
import korastudy.be.dto.request.ExamSearchRequest;
import korastudy.be.dto.request.ExamSubmissionRequest;
import korastudy.be.dto.response.ExamListResponse;
import korastudy.be.dto.response.ExamResultResponse;
import korastudy.be.dto.response.ExamStatsResponse;
import korastudy.be.payload.response.ApiSuccess;
import korastudy.be.service.IMockTestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/exams")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class MockTestController {
    
    private final IMockTestService mockTestService;
    
    /**
     * Get all mock tests with pagination
     */
    @GetMapping
    public ResponseEntity<ExamListResponse> getAllMockTests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? 
            Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        ExamListResponse response = mockTestService.getAllMockTests(pageable);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Search mock tests with filters
     */
    @PostMapping("/search")
    public ResponseEntity<ExamListResponse> searchMockTests(@RequestBody ExamSearchRequest searchRequest) {
        log.info("Searching mock tests with filters: {}", searchRequest);
        ExamListResponse response = mockTestService.searchMockTests(searchRequest);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get mock test by ID for exam taking (without correct answers)
     */
    @GetMapping("/{id}")
    public ResponseEntity<MockTestDTO> getMockTestForExam(@PathVariable Long id) {
        log.info("Getting mock test for exam: {}", id);
        MockTestDTO mockTest = mockTestService.getMockTestForExam(id);
        return ResponseEntity.ok(mockTest);
    }
    
    /**
     * Get mock test by ID with full details (admin only)
     */
    @GetMapping("/{id}/admin")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CONTENT_MANAGER')")
    public ResponseEntity<MockTestDTO> getMockTestWithAnswers(@PathVariable Long id) {
        log.info("Getting mock test with answers: {}", id);
        MockTestDTO mockTest = mockTestService.getMockTestById(id);
        return ResponseEntity.ok(mockTest);
    }
    
    /**
     * Get popular mock tests
     */
    @GetMapping("/popular")
    public ResponseEntity<List<MockTestDTO>> getPopularMockTests(
            @RequestParam(defaultValue = "10") int limit) {
        List<MockTestDTO> popularTests = mockTestService.getPopularMockTests(limit);
        return ResponseEntity.ok(popularTests);
    }
    
    /**
     * Submit exam answers
     */
    @PostMapping("/submit")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ExamResultResponse> submitExam(
            @Valid @RequestBody ExamSubmissionRequest submission,
            Authentication authentication) {
        
        String username = authentication.getName();
        log.info("Processing exam submission for user: {} and test: {}", username, submission.getMockTestId());
        
        ExamResultResponse result = mockTestService.submitExam(username, submission);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
    
    /**
     * Get exam result by ID
     */
    @GetMapping("/results/{resultId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ExamResultResponse> getExamResult(
            @PathVariable Long resultId,
            Authentication authentication) {
        
        String username = authentication.getName();
        log.info("Getting exam result: {} for user: {}", resultId, username);
        
        ExamResultResponse result = mockTestService.getExamResult(resultId, username);
        return ResponseEntity.ok(result);
    }
    
    /**
     * Get user's exam history
     */
    @GetMapping("/history")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<TestResultDTO>> getUserExamHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        
        String username = authentication.getName();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "testDate"));
        
        List<TestResultDTO> history = mockTestService.getUserExamHistory(username, pageable);
        return ResponseEntity.ok(history);
    }
    
    /**
     * Get user's exam statistics
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ExamStatsResponse> getUserExamStats(Authentication authentication) {
        String username = authentication.getName();
        log.info("Getting exam stats for user: {}", username);
        
        ExamStatsResponse stats = mockTestService.getUserExamStats(username);
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Get user's recent exam results
     */
    @GetMapping("/recent")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<TestResultDTO>> getRecentResults(
            @RequestParam(defaultValue = "5") int limit,
            Authentication authentication) {
        
        String username = authentication.getName();
        List<TestResultDTO> recentResults = mockTestService.getRecentResults(username, limit);
        return ResponseEntity.ok(recentResults);
    }
    
    /**
     * Get global exam statistics (admin only)
     */
    @GetMapping("/stats/global")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CONTENT_MANAGER')")
    public ResponseEntity<ExamStatsResponse> getGlobalExamStats() {
        log.info("Getting global exam statistics");
        ExamStatsResponse stats = mockTestService.getGlobalExamStats();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Get exam results by mock test ID (admin only)
     */
    @GetMapping("/{mockTestId}/results")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CONTENT_MANAGER')")
    public ResponseEntity<List<TestResultDTO>> getResultsByMockTestId(
            @PathVariable Long mockTestId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "testDate"));
        List<TestResultDTO> results = mockTestService.getResultsByMockTestId(mockTestId, pageable);
        return ResponseEntity.ok(results);
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<ApiSuccess> healthCheck() {
        return ResponseEntity.ok(ApiSuccess.of("Mock Test Service is running"));
    }
}
