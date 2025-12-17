package korastudy.be.mapper;

import korastudy.be.dto.response.quiz.*;
import korastudy.be.entity.Course.Quiz;
import korastudy.be.entity.Course.Question;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class QuizAdminMapper {

    // ==================== SINGLE MAPPING ====================

    /**
     * Map Quiz → QuizDTO (cho Admin/Teacher - có đáp án đầy đủ)
     */
    public static QuizDTO toQuizDTO(Quiz quiz) {
        if (quiz == null) return null;

        List<QuestionDTO> questions = null;
        if (quiz.getQuestions() != null) {
            questions = QuestionMapper.toDTOsForTeacher(quiz.getQuestions());
        }

        return QuizDTO.builder().id(quiz.getId()).title(quiz.getTitle()).description(quiz.getDescription()).timeLimit(quiz.getTimeLimit()).passingScore(quiz.getPassingScore()).isPublished(quiz.getIsPublished()).isActive(quiz.getIsActive()).totalPoints(calculateTotalPoints(quiz)).sectionId(quiz.getSection() != null ? quiz.getSection().getId() : null).sectionName(quiz.getSection() != null ? quiz.getSection().getSectionName() : null).questions(questions).questionCount(quiz.getQuestions() != null ? quiz.getQuestions().size() : 0).createdAt(quiz.getCreatedAt()).updatedAt(quiz.getLastModified()).build();
    }

    /**
     * Map Quiz → QuizBasicInfoDTO (cho Admin/Teacher)
     */
    public static QuizBasicInfoDTO toBasicInfoDTO(Quiz quiz) {
        if (quiz == null) return null;

        return QuizBasicInfoDTO.builder().id(quiz.getId()).title(quiz.getTitle()).description(quiz.getDescription()).timeLimit(quiz.getTimeLimit()).passingScore(quiz.getPassingScore()).isPublished(quiz.getIsPublished()).isActive(quiz.getIsActive()).totalPoints(calculateTotalPoints(quiz)).sectionId(quiz.getSection() != null ? quiz.getSection().getId() : null).sectionName(quiz.getSection() != null ? quiz.getSection().getSectionName() : null).questionCount(quiz.getQuestions() != null ? quiz.getQuestions().size() : 0).createdAt(quiz.getCreatedAt()).updatedAt(quiz.getLastModified()).build();
    }

    /**
     * Map Quiz → QuizSummaryDTO (cho Admin/Teacher)
     */
    public static QuizSummaryDTO toSummaryDTO(Quiz quiz) {
        if (quiz == null) return null;

        return QuizSummaryDTO.builder().id(quiz.getId()).title(quiz.getTitle()).description(quiz.getDescription()).timeLimit(quiz.getTimeLimit()).passingScore(quiz.getPassingScore()).questionCount(quiz.getQuestions() != null ? quiz.getQuestions().size() : 0).createdAt(quiz.getCreatedAt()).isPublished(quiz.getIsPublished()).isActive(quiz.getIsActive()).sectionId(quiz.getSection() != null ? quiz.getSection().getId() : null).sectionName(quiz.getSection() != null ? quiz.getSection().getSectionName() : null).build();
    }

    /**
     * Map Quiz → QuizStatusDTO (cho Admin/Teacher)
     */
    public static QuizStatusDTO toStatusDTO(Quiz quiz) {
        if (quiz == null) return null;

        return QuizStatusDTO.builder().quizId(quiz.getId()).quizTitle(quiz.getTitle()).timeLimit(quiz.getTimeLimit()).passingScore(quiz.getPassingScore()).isAvailable(quiz.getIsPublished() && quiz.getIsActive()).canRetake(true)  // Mặc định cho phép retake
                // Các field sau sẽ được set trong service:
                .isCompleted(false).attemptCount(0).bestScore(0.0).isPassed(false).lastAttemptDate(null).build();
    }

    // ==================== LIST MAPPING ====================

    /**
     * Map List<Quiz> → List<QuizSummaryDTO> (cho Admin/Teacher)
     */
    public static List<QuizSummaryDTO> toSummaryDTOs(List<Quiz> quizzes) {
        if (quizzes == null) return null;

        return quizzes.stream().map(QuizAdminMapper::toSummaryDTO).collect(Collectors.toList());
    }

    /**
     * Map List<Quiz> → List<QuizStatusDTO> (cho Admin/Teacher)
     */
    public static List<QuizStatusDTO> toStatusDTOs(List<Quiz> quizzes) {
        if (quizzes == null) return null;

        return quizzes.stream().map(QuizAdminMapper::toStatusDTO).collect(Collectors.toList());
    }

    // ==================== HELPER METHOD ====================

    private static Integer calculateTotalPoints(Quiz quiz) {
        if (quiz.getQuestions() == null) return 0;

        return quiz.getQuestions().stream().mapToInt(q -> q.getScore() != null ? q.getScore().intValue() : 0).sum();
    }
}