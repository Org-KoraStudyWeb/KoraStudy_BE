package korastudy.be.mapper;

import korastudy.be.dto.response.quiz.TestResultDTO;
import korastudy.be.dto.response.quiz.AnswerResultDTO;
import korastudy.be.entity.Course.TestResult;
import korastudy.be.entity.Course.QuizAnswer;

import java.util.List;
import java.util.stream.Collectors;

public class TestResultMapper {

    // ==================== SINGLE MAPPING ====================

    /**
     * Map TestResult → TestResultDTO (cơ bản)
     */
    public static TestResultDTO toDTO(TestResult testResult) {
        if (testResult == null) return null;

        return TestResultDTO.builder().id(testResult.getId()).score(testResult.getScore()).earnedPoints(testResult.getEarnedPoints()).totalPoints(testResult.getTotalPoints()).totalQuestions(testResult.getTotalQuestions()).correctAnswers(testResult.getCorrectAnswers()).passingScore(testResult.getQuiz() != null ? testResult.getQuiz().getPassingScore() : null).isPassed(testResult.getIsPassed()).takenDate(testResult.getTakenDate()).timeSpent(testResult.getTimeSpent()).quizId(testResult.getQuiz() != null ? testResult.getQuiz().getId() : null).quizTitle(testResult.getQuiz() != null ? testResult.getQuiz().getTitle() : null).userId(testResult.getUser() != null ? testResult.getUser().getId() : null).username(testResult.getUser() != null && testResult.getUser().getAccount() != null ? testResult.getUser().getAccount().getUsername() : null).answerDetails(null)  // Cần set riêng nếu có
                .build();
    }

    /**
     * Map TestResult + List<QuizAnswer> → TestResultDTO (đầy đủ)
     */
    public static TestResultDTO toDTOWithDetails(TestResult testResult, List<QuizAnswer> quizAnswers) {
        if (testResult == null) return null;

        List<AnswerResultDTO> answerDetails = null;
        if (quizAnswers != null) {
            answerDetails = quizAnswers.stream().map(QuizAnswerMapper::toAnswerResultDTO).collect(Collectors.toList());
        }

        return TestResultDTO.builder().id(testResult.getId()).score(testResult.getScore()).earnedPoints(testResult.getEarnedPoints()).totalPoints(testResult.getTotalPoints()).totalQuestions(testResult.getTotalQuestions()).correctAnswers(testResult.getCorrectAnswers()).passingScore(testResult.getQuiz() != null ? testResult.getQuiz().getPassingScore() : null).isPassed(testResult.getIsPassed()).takenDate(testResult.getTakenDate()).timeSpent(testResult.getTimeSpent()).quizId(testResult.getQuiz() != null ? testResult.getQuiz().getId() : null).quizTitle(testResult.getQuiz() != null ? testResult.getQuiz().getTitle() : null).userId(testResult.getUser() != null ? testResult.getUser().getId() : null).username(testResult.getUser() != null && testResult.getUser().getAccount() != null ? testResult.getUser().getAccount().getUsername() : null).answerDetails(answerDetails).build();
    }
}