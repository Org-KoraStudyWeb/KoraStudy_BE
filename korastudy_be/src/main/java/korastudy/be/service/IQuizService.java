package korastudy.be.service;

import korastudy.be.dto.request.quiz.*;
import korastudy.be.dto.response.quiz.*;

import java.util.List;

public interface IQuizService {

    // ==================== QUIZ QUẢN LÝ (ADMIN) ====================

    /**
     * Tạo mới quiz (Admin)
     */
    QuizDTO createQuiz(QuizCreateRequest request);

    /**
     * Cập nhật quiz (Admin)
     */
    QuizDTO updateQuiz(Long quizId, QuizUpdateRequest request);

    /**
     * Xóa quiz (Admin)
     */
    void deleteQuiz(Long quizId);

    /**
     * Bật/tắt hiển thị quiz (Admin)
     */
    void publishQuiz(Long quizId, boolean publish);

    // ==================== XEM QUIZ ====================

    /**
     * Lấy chi tiết quiz cho Admin (có đáp án)
     */
    QuizDTO getQuizForTeacher(Long quizId);

    /**
     * Lấy quiz để học sinh làm bài (ẩn đáp án)
     */
    QuizDTO getQuizForStudent(Long quizId);

    /**
     * Lấy danh sách quiz có sẵn cho học sinh
     */
    List<QuizSummaryDTO> getAvailableQuizzesForStudent(Long sectionId, Long userId);

    /**
     * Lấy trạng thái quiz của học sinh (đã làm chưa, điểm, v.v.)
     */
    QuizStatusDTO getQuizStatusForStudent(Long quizId, Long userId);

    /**
     * Lấy danh sách quiz theo section (Admin)
     */
    List<QuizSummaryDTO> getQuizzesBySectionId(Long sectionId);

    // ==================== QUẢN LÝ CÂU HỎI ====================

    /**
     * Thêm câu hỏi vào quiz
     */
    QuestionDTO addQuestionToQuiz(Long quizId, QuestionCreateRequest request);

    /**
     * Thêm nhiều câu hỏi vào quiz (Import Excel)
     */
    List<QuestionDTO> addQuestionsToQuiz(Long quizId, List<QuestionCreateRequest> requests);

    /**
     * Cập nhật câu hỏi
     */
    QuestionDTO updateQuestion(Long questionId, QuestionUpdateRequest request);

    /**
     * Xóa câu hỏi
     */
    void deleteQuestion(Long questionId);

    /**
     * Lấy danh sách câu hỏi (Admin view - có đáp án)
     */
    List<QuestionDTO> getQuestionsForTeacher(Long quizId);

    /**
     * Lấy danh sách câu hỏi (Học sinh view - ẩn đáp án)
     */
    List<QuestionDTO> getQuestionsForStudent(Long quizId);

    // ==================== QUẢN LÝ ĐÁP ÁN (OPTIONS) ====================

    /**
     * Thêm đáp án vào câu hỏi
     */
    OptionDTO addOptionToQuestion(Long questionId, OptionCreateRequest request);

    /**
     * Cập nhật đáp án
     */
    OptionDTO updateOption(Long optionId, OptionUpdateRequest request);

    /**
     * Xóa đáp án
     */
    void deleteOption(Long optionId);

    /**
     * Lấy danh sách đáp án của câu hỏi
     */
    List<OptionDTO> getOptionsByQuestionId(Long questionId);

    // ==================== LÀM BÀI THI ====================

    /**
     * Bắt đầu làm quiz (tạo TestResult)
     */
    TestResultDTO startQuiz(Long quizId, Long userId);

    /**
     * Nộp bài và chấm điểm
     */
    TestResultDTO submitQuiz(Long quizId, QuizSubmissionRequest request, Long userId);

    // ==================== KẾT QUẢ ====================

    /**
     * Lấy kết quả bài làm theo ID
     */
    TestResultDTO getQuizResult(Long resultId);

    /**
     * Lấy chi tiết kết quả cho học sinh
     */
    QuizResultDetailDTO getQuizResultForStudent(Long resultId, Long userId);

    /**
     * Lấy chi tiết kết quả cho Admin
     */
    QuizResultDetailDTO getQuizResultForTeacher(Long resultId);

    /**
     * Lấy lịch sử làm bài của user
     */
    List<TestResultDTO> getUserQuizHistory(Long userId);

    /**
     * Lấy danh sách kết quả của một quiz (Admin)
     */
    List<TestResultDTO> getQuizResults(Long quizId);

    /**
     * Lấy thống kê quiz (Admin)
     */
    QuizStatisticsDTO getQuizStatistics(Long quizId);

    // ==================== KIỂM TRA ====================

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

// ==================== TIẾN ĐỘ QUIZ THEO COURSE ====================

    /**
     * Lấy tiến độ quiz của user trong một course
     */
    List<UserQuizProgressInCourseDTO> getUserQuizProgressInCourse(Long userId, Long courseId);

    /**
     * Lấy thống kê tiến độ quiz của user trong course
     */
    UserQuizProgressSummaryDTO getUserQuizProgressSummary(Long userId, Long courseId);

    List<UserQuizProgressSummaryDTO> getAllUsersQuizProgressInCourse(Long courseId);

    UserQuizDetailedAverageScoreDTO getUserDetailedAverageScoreInCourse(Long userId, Long courseId);

    Double getUserSimpleAverageScoreInCourse(Long userId, Long courseId);
}