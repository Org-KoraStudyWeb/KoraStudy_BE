package korastudy.be.service.impl;

import korastudy.be.dto.exam.*;
import korastudy.be.dto.request.ExamSearchRequest;
import korastudy.be.dto.request.ExamSubmissionRequest;
import korastudy.be.dto.response.ExamListResponse;
import korastudy.be.dto.response.ExamResultResponse;
import korastudy.be.dto.response.ExamStatsResponse;
import korastudy.be.entity.MockTest.*;
import korastudy.be.entity.User.User;
import korastudy.be.exception.ResourceNotFoundException;
import korastudy.be.repository.MockTestRepository;
import korastudy.be.repository.PracticeTestResultRepository;
import korastudy.be.service.IMockTestService;
import korastudy.be.service.IUserService;
import korastudy.be.util.ExamMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MockTestService implements IMockTestService {
    
    private final MockTestRepository mockTestRepository;
    private final PracticeTestResultRepository practiceTestResultRepository;
    private final IUserService userService;
    private final ExamMapper examMapper;
    
    @Override
    @Transactional(readOnly = true)
    public ExamListResponse getAllMockTests(Pageable pageable) {
        Page<MockTest> mockTestPage = mockTestRepository.findAll(pageable);
        return buildExamListResponse(mockTestPage);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ExamListResponse searchMockTests(ExamSearchRequest searchRequest) {
        Pageable pageable = buildPageable(searchRequest);
        Page<MockTest> mockTestPage;
        
        if (searchRequest.getDifficulty() != null && !searchRequest.getDifficulty().trim().isEmpty()) {
            mockTestPage = mockTestRepository.findByDifficulty(searchRequest.getDifficulty(), pageable);
        } else {
            mockTestPage = mockTestRepository.searchMockTests(
                searchRequest.getTitle(),
                searchRequest.getLevel(),
                searchRequest.getMinQuestions(),
                searchRequest.getMaxQuestions(),
                pageable
            );
        }
        
        return buildExamListResponse(mockTestPage);
    }
    
    @Override
    @Transactional(readOnly = true)
    public MockTestDTO getMockTestById(Long id) {
        MockTest mockTest = mockTestRepository.findByIdWithPartsAndQuestions(id)
            .orElseThrow(() -> new ResourceNotFoundException("Mock test not found with id: " + id));
        
        return examMapper.toDTO(mockTest, true);
    }
    
    @Override
    @Transactional(readOnly = true)
    public MockTestDTO getMockTestForExam(Long id) {
        MockTest mockTest = mockTestRepository.findByIdWithPartsAndQuestions(id)
            .orElseThrow(() -> new ResourceNotFoundException("Mock test not found with id: " + id));
        
        return examMapper.toDTO(mockTest, false); // Hide correct answers
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MockTestDTO> getPopularMockTests(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<MockTest> popularTests = mockTestRepository.findPopularMockTests(pageable);
        
        return popularTests.stream()
            .map(test -> examMapper.toDTO(test, false))
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public ExamResultResponse submitExam(String username, ExamSubmissionRequest submission) {
        log.info("Processing exam submission for user: {} and test: {}", username, submission.getMockTestId());
        
        // Get user and mock test
        User user = userService.getUserByAccountUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        
        MockTest mockTest = mockTestRepository.findByIdWithPartsAndQuestions(submission.getMockTestId())
            .orElseThrow(() -> new ResourceNotFoundException("Mock test not found: " + submission.getMockTestId()));
        
        // Calculate results
        int correctAnswers = 0;
        int totalQuestions = submission.getAnswers().size();
        List<ExamResultResponse.QuestionResultDetail> questionDetails = new ArrayList<>();
        
        for (ExamSubmissionRequest.AnswerSubmissionRequest answer : submission.getAnswers()) {
            MockTestQuestion question = findQuestionById(mockTest, answer.getQuestionId());
            MockTestAnswers correctAnswer = findCorrectAnswer(question);
            
            boolean isCorrect = correctAnswer != null && 
                correctAnswer.getSelectedAnswer().equals(answer.getSelectedAnswer());
            
            if (isCorrect) {
                correctAnswers++;
            }
            
            questionDetails.add(ExamResultResponse.QuestionResultDetail.builder()
                .questionId(question.getId())
                .questionText(question.getQuestionText())
                .userAnswer(answer.getSelectedAnswer())
                .correctAnswer(correctAnswer != null ? correctAnswer.getSelectedAnswer() : "N/A")
                .isCorrect(isCorrect)
                .build());
        }
        
        // Save result
        PracticeTestResult result = PracticeTestResult.builder()
            .user(user)
            .mockTest(mockTest)
            .testType(submission.getTestType())
            .testDate(LocalDateTime.now())
            .noCorrect(correctAnswers)
            .noIncorrect(totalQuestions - correctAnswers)
            .build();
        
        result = practiceTestResultRepository.save(result);
        
        // Build response
        double score = totalQuestions > 0 ? (double) correctAnswers / totalQuestions * 100 : 0;
        
        return ExamResultResponse.builder()
            .resultId(result.getId())
            .mockTestId(mockTest.getId())
            .testTitle(mockTest.getTitle())
            .testType(submission.getTestType())
            .testDate(result.getTestDate())
            .totalQuestions(totalQuestions)
            .correctAnswers(correctAnswers)
            .incorrectAnswers(totalQuestions - correctAnswers)
            .score((double) correctAnswers)
            .percentage(score)
            .durationMinutes(submission.getDurationMinutes())
            .level(mockTest.getLevel())
            .grade(examMapper.calculateGrade(score))
            .questionDetails(questionDetails)
            .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public ExamResultResponse getExamResult(Long resultId, String username) {
        PracticeTestResult result = practiceTestResultRepository.findByIdAndUserUsername(resultId, username)
            .orElseThrow(() -> new ResourceNotFoundException("Exam result not found: " + resultId));
        
        MockTest mockTest = result.getMockTest();
        int totalQuestions = result.getNoCorrect() + result.getNoIncorrect();
        double percentage = totalQuestions > 0 ? (double) result.getNoCorrect() / totalQuestions * 100 : 0;
        
        return ExamResultResponse.builder()
            .resultId(result.getId())
            .mockTestId(mockTest.getId())
            .testTitle(mockTest.getTitle())
            .testType(result.getTestType())
            .testDate(result.getTestDate())
            .totalQuestions(totalQuestions)
            .correctAnswers(result.getNoCorrect())
            .incorrectAnswers(result.getNoIncorrect())
            .score(result.getNoCorrect().doubleValue())
            .percentage(percentage)
            .level(mockTest.getLevel())
            .grade(examMapper.calculateGrade(percentage))
            .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TestResultDTO> getUserExamHistory(String username, Pageable pageable) {
        Page<PracticeTestResult> results = practiceTestResultRepository.findByUserUsername(username, pageable);
        
        return results.getContent().stream()
            .map(examMapper::toTestResultDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public ExamStatsResponse getUserExamStats(String username) {
        Long totalAttempts = practiceTestResultRepository.countByUserUsername(username);
        Double averageScore = practiceTestResultRepository.getAverageScoreByUsername(username).orElse(0.0);
        List<TestResultDTO> recentResults = getRecentResults(username, 5);
        
        // Get performance by level
        List<Object[]> levelPerformance = practiceTestResultRepository.getPerformanceByLevelAndUsername(username);
        Map<String, Double> levelAverageScores = new HashMap<>();
        Map<String, Long> levelDistribution = new HashMap<>();
        
        for (Object[] row : levelPerformance) {
            String level = (String) row[0];
            Double avgScore = (Double) row[1];
            Long count = (Long) row[2];
            
            levelAverageScores.put(level, avgScore);
            levelDistribution.put(level, count);
        }
        
        String mostPopularLevel = levelDistribution.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("N/A");
        
        return ExamStatsResponse.builder()
            .totalAttempts(totalAttempts)
            .averageScore(averageScore)
            .mostPopularLevel(mostPopularLevel)
            .levelDistribution(levelDistribution)
            .levelAverageScores(levelAverageScores)
            .recentResults(recentResults)
            .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public ExamStatsResponse getGlobalExamStats() {
        Long totalExams = mockTestRepository.count();
        Object[] globalStats = practiceTestResultRepository.getGlobalStats();
        Long totalAttempts = (Long) globalStats[0];
        Double averageScore = (Double) globalStats[1];
        
        List<Object[]> levelDist = practiceTestResultRepository.getLevelDistribution();
        Map<String, Long> levelDistribution = levelDist.stream()
            .collect(Collectors.toMap(
                row -> (String) row[0],
                row -> (Long) row[1]
            ));
        
        List<String> popularLevels = practiceTestResultRepository.getMostPopularLevels(PageRequest.of(0, 1));
        String mostPopularLevel = popularLevels.isEmpty() ? "N/A" : popularLevels.get(0);
        
        return ExamStatsResponse.builder()
            .totalExams(totalExams)
            .totalAttempts(totalAttempts)
            .averageScore(averageScore != null ? averageScore : 0.0)
            .mostPopularLevel(mostPopularLevel)
            .levelDistribution(levelDistribution)
            .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TestResultDTO> getRecentResults(String username, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<PracticeTestResult> results = practiceTestResultRepository.findRecentByUserUsername(username, pageable);
        
        return results.stream()
            .map(examMapper::toTestResultDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TestResultDTO> getResultsByMockTestId(Long mockTestId, Pageable pageable) {
        Page<PracticeTestResult> results = practiceTestResultRepository.findByMockTestId(mockTestId, pageable);
        
        return results.getContent().stream()
            .map(examMapper::toTestResultDTO)
            .collect(Collectors.toList());
    }
    
    // Helper methods
    private ExamListResponse buildExamListResponse(Page<MockTest> mockTestPage) {
        List<MockTestDTO> content = mockTestPage.getContent().stream()
            .map(test -> examMapper.toDTO(test, false))
            .collect(Collectors.toList());
        
        return ExamListResponse.builder()
            .content(content)
            .totalPages(mockTestPage.getTotalPages())
            .totalElements(mockTestPage.getTotalElements())
            .currentPage(mockTestPage.getNumber())
            .size(mockTestPage.getSize())
            .first(mockTestPage.isFirst())
            .last(mockTestPage.isLast())
            .hasNext(mockTestPage.hasNext())
            .hasPrevious(mockTestPage.hasPrevious())
            .build();
    }
    
    private Pageable buildPageable(ExamSearchRequest searchRequest) {
        int page = searchRequest.getPage() != null ? searchRequest.getPage() : 0;
        int size = searchRequest.getSize() != null ? searchRequest.getSize() : 10;
        
        Sort sort = Sort.by("createdAt").descending();
        if (searchRequest.getSortBy() != null && !searchRequest.getSortBy().trim().isEmpty()) {
            Sort.Direction direction = "desc".equalsIgnoreCase(searchRequest.getSortDirection()) ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
            sort = Sort.by(direction, searchRequest.getSortBy());
        }
        
        return PageRequest.of(page, size, sort);
    }
    
    private MockTestQuestion findQuestionById(MockTest mockTest, Long questionId) {
        return mockTest.getParts().stream()
            .flatMap(part -> part.getQuestions().stream())
            .filter(question -> question.getId().equals(questionId))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Question not found: " + questionId));
    }
    
    private MockTestAnswers findCorrectAnswer(MockTestQuestion question) {
        return question.getAnswers().stream()
            .filter(answer -> Boolean.TRUE.equals(answer.getIsCorrect()))
            .findFirst()
            .orElse(null);
    }
}
