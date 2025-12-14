package korastudy.be.service;

import korastudy.be.dto.request.quiz.*;
import korastudy.be.dto.response.quiz.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface IQuizService {

    // ==================== QUIZ CRUD (TEACHER/ADMIN) ====================

    /**
     * Tạo mới quiz (Teacher/Admin)
     */
    QuizDTO createQuiz(QuizCreateRequest request);

    /**
     * Cập nhật quiz (Teacher/Admin)
     */
    QuizDTO updateQuiz(Long quizId, QuizUpdateRequest request);

    /**
     * Xóa quiz (Teacher/Admin)
     */
    void deleteQuiz(Long quizId);

    /**
     * Publish/Unpublish quiz (Teacher/Admin)
     */
    void publishQuiz(Long quizId, boolean publish);

    // ==================== QUIZ VIEW (TEACHER/ADMIN) ====================

    /**
     * Lấy chi tiết quiz cho Teacher/Admin (có đáp án)
     */
    QuizDTO getQuizForTeacher(Long quizId);

    /**
     * Lấy thông tin cơ bản quiz
     */
    QuizBasicInfoDTO getQuizBasicInfo(Long quizId);

    /**
     * Lấy tóm tắt quiz
     */
    QuizSummaryDTO getQuizSummary(Long quizId);

    /**
     * Lấy danh sách quiz theo section (Teacher/Admin)
     */
    List<QuizSummaryDTO> getQuizzesBySectionId(Long sectionId);

    /**
     * Tìm kiếm quiz (Teacher/Admin)
     */
    List<QuizSummaryDTO> searchQuizzes(QuizSearchRequest request);

    // ==================== QUIZ VIEW (STUDENT) ====================

    /**
     * Lấy quiz để làm bài cho Student (ẩn đáp án)
     */
    QuizDTO getQuizForStudent(Long quizId);

    /**
     * Lấy danh sách quiz có sẵn cho Student
     */
    List<QuizSummaryDTO> getAvailableQuizzesForStudent(Long sectionId, Long userId);

    /**
     * Lấy trạng thái quiz cho Student (đã làm chưa, điểm cao nhất, v.v.)
     */
    QuizStatusDTO getQuizStatusForStudent(Long quizId, Long userId);

    // ==================== QUESTION MANAGEMENT ====================

    /**
     * Thêm câu hỏi vào quiz
     */
    QuestionDTO addQuestionToQuiz(Long quizId, QuestionCreateRequest request);

    /**
     * Cập nhật câu hỏi
     */
    QuestionDTO updateQuestion(Long questionId, QuestionUpdateRequest request);

    /**
     * Xóa câu hỏi
     */
    void deleteQuestion(Long questionId);

    /**
     * Lấy danh sách câu hỏi (Teacher view - có đáp án)
     */
    List<QuestionDTO> getQuestionsForTeacher(Long quizId);

    /**
     * Lấy danh sách câu hỏi (Student view - ẩn đáp án)
     */
    List<QuestionDTO> getQuestionsForStudent(Long quizId);

    // ==================== OPTION MANAGEMENT ====================

    /**
     * Thêm option vào câu hỏi
     */
    OptionDTO addOptionToQuestion(Long questionId, OptionCreateRequest request);

    /**
     * Cập nhật option
     */
    OptionDTO updateOption(Long optionId, OptionUpdateRequest request);

    /**
     * Xóa option
     */
    void deleteOption(Long optionId);

    /**
     * Lấy danh sách option của câu hỏi
     */
    List<OptionDTO> getOptionsByQuestionId(Long questionId);

    // ==================== QUIZ TAKING & SUBMISSION ====================

    /**
     * Bắt đầu làm quiz (tạo TestResult record)
     */
    TestResultDTO startQuiz(Long quizId, Long userId);

    /**
     * Nộp bài và chấm điểm
     */
    TestResultDTO submitQuiz(Long quizId, QuizSubmissionRequest request, Long userId);

    /**
     * Lưu câu trả lời tạm thời (nếu cần resume)
     */
    void saveAnswer(Long quizId, AnswerRequest request, Long userId);

    // ==================== RESULTS & ANALYTICS ====================

    /**
     * Lấy kết quả bài làm theo ID
     */
    TestResultDTO getQuizResult(Long resultId);

    /**
     * Lấy chi tiết đầy đủ kết quả bài làm (bao gồm thông tin quiz)
     */
    QuizResultDetailDTO getQuizResultDetail(Long resultId);

    /**
     * Lấy kết quả bài làm cho Student (Student view)
     */
    QuizResultDetailDTO getQuizResultForStudent(Long resultId, Long userId);

    /**
     * Lấy kết quả bài làm cho Teacher (Teacher view - đầy đủ)
     */
    QuizResultDetailDTO getQuizResultForTeacher(Long resultId);

    /**
     * Lấy lịch sử làm bài của user
     */
    List<TestResultDTO> getUserQuizHistory(Long userId);

    /**
     * Lấy danh sách kết quả của một quiz
     */
    List<TestResultDTO> getQuizResults(Long quizId);

    /**
     * Lấy tất cả kết quả của một quiz (Teacher view - chi tiết)
     */
    QuizAllResultsDTO getAllQuizResults(Long quizId);

    /**
     * Lấy thống kê quiz (Teacher view)
     */
    QuizStatisticsDTO getQuizStatistics(Long quizId);

    /**
     * Lấy thống kê của tất cả quiz trong section
     */
    List<QuizStatisticsDTO> getQuizStatisticsBySectionId(Long sectionId);

    // ==================== UTILITY METHODS ====================

    /**
     * Kiểm tra quiz có tồn tại không
     */
    boolean existsQuiz(Long quizId);

    /**
     * Kiểm tra section có quiz nào không
     */
    boolean existsAnyQuizBySectionId(Long sectionId);

    /**
     * Kiểm tra user có quyền truy cập quiz không
     */
    boolean canUserAccessQuiz(Long quizId, Long userId);

    /**
     * Đếm số quiz trong section
     */
    long countQuizzesBySectionId(Long sectionId);

    /**
     * Validate quiz trước khi làm bài
     */
    void validateQuizForTaking(Long quizId, Long userId);
}