package korastudy.be.mapper;

import korastudy.be.dto.response.quiz.QuestionDTO;
import korastudy.be.dto.response.quiz.OptionDTO;
import korastudy.be.entity.Course.Question;
import korastudy.be.entity.Enum.QuestionType;

import java.util.List;
import java.util.stream.Collectors;

public class QuestionMapper {

    // ==================== SINGLE MAPPING ====================

    /**
     * Map Question → QuestionDTO (cho Teacher/Admin - có đáp án đầy đủ)
     */
    public static QuestionDTO toDTOForTeacher(Question question) {
        if (question == null) return null;

        List<OptionDTO> options = null;
        if (question.getOptions() != null) {
            options = OptionMapper.toDTOs(question.getOptions());
        }

        QuestionDTO.QuestionDTOBuilder builder = QuestionDTO.builder()
                .id(question.getId())
                .questionText(question.getQuestionText())
                .questionType(question.getQuestionType())
                .score(question.getScore())
                .orderIndex(question.getOrderIndex())
                .imageUrl(question.getImageUrl())
                .explanation(question.getExplanation())
                .showCorrectAnswer(true)  // Teacher xem được đáp án
                .options(options);

        // Thay vì dùng getCorrectAnswer(), giờ dùng options để lấy correctAnswer
        String correctAnswer = getCorrectAnswerFromOptions(question);
        builder.correctAnswer(correctAnswer);

        return builder.build();
    }

    /**
     * Map Question → QuestionDTO (cho Student - ẩn đáp án)
     */
    public static QuestionDTO toDTOForStudent(Question question) {
        if (question == null) return null;

        List<OptionDTO> options = null;
        if (question.getOptions() != null) {
            options = OptionMapper.toDTOsForStudent(question.getOptions());
        }

        QuestionDTO.QuestionDTOBuilder builder = QuestionDTO.builder()
                .id(question.getId())
                .questionText(question.getQuestionText())
                .questionType(question.getQuestionType())
                .score(question.getScore())
                .orderIndex(question.getOrderIndex())
                .imageUrl(question.getImageUrl())
                .showCorrectAnswer(false)  // Student không xem được đáp án
                .options(options);

        // Student không xem được explanation và correctAnswer
        // (sẽ được set sau khi nộp bài trong AnswerResultDTO)

        return builder.build();
    }

    // ==================== LIST MAPPING ====================

    /**
     * Map List<Question> → List<QuestionDTO> (cho Teacher/Admin)
     */
    public static List<QuestionDTO> toDTOsForTeacher(List<Question> questions) {
        if (questions == null) return null;

        return questions.stream()
                .map(QuestionMapper::toDTOForTeacher)
                .collect(Collectors.toList());
    }

    /**
     * Map List<Question> → List<QuestionDTO> (cho Student)
     */
    public static List<QuestionDTO> toDTOsForStudent(List<Question> questions) {
        if (questions == null) return null;

        return questions.stream()
                .map(QuestionMapper::toDTOForStudent)
                .collect(Collectors.toList());
    }

    // ==================== HELPER METHODS ====================

    private static String getCorrectAnswerFromOptions(Question question) {
        if (question == null || question.getOptions() == null || question.getOptions().isEmpty()) {
            return "";
        }

        // Với tất cả loại câu hỏi, lấy đáp án đúng từ options
        List<String> correctOptions = question.getOptions().stream()
                .filter(opt -> opt != null && Boolean.TRUE.equals(opt.getIsCorrect()))
                .map(opt -> opt.getOptionText())
                .collect(Collectors.toList());

        if (correctOptions.isEmpty()) {
            return "";
        }

        // Nối các đáp án đúng bằng dấu phẩy
        return String.join(", ", correctOptions);
    }
}