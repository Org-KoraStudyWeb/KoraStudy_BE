package korastudy.be.service;

import korastudy.be.dto.request.quiz.*;
import korastudy.be.dto.response.quiz.*;
import korastudy.be.entity.Course.Question;
import korastudy.be.entity.Course.Quiz;

import java.util.List;

public interface IQuizService {

    // ==================== QUIZ CRUD ====================

    /**
     * Tạo quiz mới cho một SECTION
     * - Validate section có tồn tại không
     * - Một section có thể có nhiều quiz
     */
    QuizDTO createQuiz(QuizCreateRequest request);

    /**
     * Cập nhật thông tin quiz (title, description, timeLimit, passingScore)
     * - KHÔNG update questions qua API này (dùng API riêng cho questions)
     */
    QuizDTO updateQuiz(Long quizId, QuizUpdateRequest request);

    /**
     * Lấy chi tiết quiz theo ID (dành cho giáo viên/quản trị)
     * - Bao gồm toàn bộ questions và correct answers
     */
    QuizDTO getQuizById(Long id);

    /**
     * Lấy DANH SÁCH quiz theo SECTION ID
     * - Một section có thể có nhiều quiz
     */
    List<QuizDTO> getQuizzesBySectionId(Long sectionId);

    /**
     * Lấy quiz cụ thể theo SECTION ID và QUIZ ID
     */
    QuizDTO getQuizBySectionAndId(Long sectionId, Long quizId);

    /**
     * Xóa quiz và tất cả questions, options liên quan
     * - Cascade delete
     */
    void deleteQuiz(Long id);

    // ==================== QUESTION MANAGEMENT ====================

    /**
     * Thêm câu hỏi mới vào quiz
     * - Validate quiz tồn tại
     * - Tạo question với các options
     */
    QuestionDTO addQuestionToQuiz(Long quizId, QuestionCreateRequest request);

    /**
     * Cập nhật câu hỏi và options
     * - Có thể thay đổi loại câu hỏi (single/multiple choice)
     */
    QuestionDTO updateQuestion(Long questionId, QuestionUpdateRequest request);

    /**
     * Xóa câu hỏi khỏi quiz
     * - Tự động xóa options liên quan
     */
    void deleteQuestion(Long questionId);

    /**
     * Lấy danh sách câu hỏi của quiz
     * - Dành cho giáo viên chỉnh sửa
     * - Bao gồm correct answers
     */
    List<QuestionDTO> getQuestionsByQuizId(Long quizId);

    // ==================== QUIZ TAKING & SUBMISSION ====================

    /**
     * Lấy quiz để làm bài (dành cho học viên)
     * - KHÔNG bao gồm correct answers
     * - Chỉ hiển thị questions và options (không có isCorrect)
     */
    QuizDTO getQuizForTaking(Long quizId);

    /**
     * Lấy DANH SÁCH quiz để làm bài theo SECTION ID
     * - Tiện lợi cho frontend khi chỉ biết sectionId
     */
    List<QuizDTO> getQuizzesForTakingBySectionId(Long sectionId);

    /**
     * Lấy quiz MẶC ĐỊNH để làm bài theo SECTION ID
     * - Trả về quiz đầu tiên hoặc quiz được đánh dấu là mặc định
     */
    QuizDTO getDefaultQuizForTakingBySectionId(Long sectionId);

    /**
     * Nộp bài quiz và chấm điểm
     * - Tính điểm tự động
     * - Lưu kết quả vào TestResult
     * - Validate thời gian làm bài
     */
    TestResultDTO submitQuiz(Long quizId, QuizSubmissionRequest request, String username);

    // ==================== RESULTS & ANALYTICS ====================

    /**
     * Lấy kết quả bài test theo ID
     * - Hiển thị điểm số, thời gian làm bài
     */
    TestResultDTO getQuizResult(Long resultId);

    /**
     * Lấy chi tiết kết quả bài test
     * - Bao gồm từng câu hỏi, đáp án user chọn, đáp án đúng
     * - Dành cho review sau khi làm bài
     */
    QuizResultDetailDTO getQuizResultDetail(Long resultId);

    /**
     * Lấy lịch sử làm bài của user
     * - Hiển thị tất cả quiz user đã làm
     */
    List<TestResultDTO> getUserQuizResults(String username);

    /**
     * Lấy kết quả của tất cả user cho một quiz
     * - Dành cho giáo viên theo dõi
     */
    List<TestResultDTO> getQuizResults(Long quizId);

    /**
     * Lấy thống kê chi tiết của quiz
     * - Số người làm, điểm trung bình, tỷ lệ đỗ, v.v.
     */
    QuizStatisticsDTO getQuizStatistics(Long quizId);

    /**
     * Lấy thống kê chi tiết của tất cả quiz trong section
     * - Tiện lợi cho frontend khi chỉ biết sectionId
     */
    List<QuizStatisticsDTO> getQuizStatisticsBySectionId(Long sectionId);

    // ==================== VALIDATION METHODS ====================

    /**
     * Kiểm tra section có quiz nào không
     */
    boolean existsAnyQuizBySectionId(Long sectionId);

    /**
     * Đếm số quiz trong section
     */
    long countQuizzesBySectionId(Long sectionId);

    // ==================== UTILITY METHODS ====================

    /**
     * Map Entity Quiz → DTO (cho internal use)
     */
    QuizDTO mapToDTO(Quiz quiz);

    /**
     * Map Entity Question → DTO (cho internal use)
     */
    QuestionDTO mapToDTO(Question question);
}