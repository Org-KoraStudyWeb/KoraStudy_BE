package korastudy.be.mapper;

import korastudy.be.dto.response.quiz.AnswerResultDTO;
import korastudy.be.dto.response.quiz.OptionDTO;
import korastudy.be.entity.Course.Option;
import korastudy.be.entity.Course.QuizAnswer;
import korastudy.be.entity.Enum.QuestionType;

import java.util.List;
import java.util.stream.Collectors;

public class QuizAnswerMapper {

    // ==================== SINGLE MAPPING ====================

    public static AnswerResultDTO toAnswerResultDTO(QuizAnswer quizAnswer) {
        if (quizAnswer == null) return null;

        AnswerResultDTO.AnswerResultDTOBuilder builder = AnswerResultDTO.builder().questionId(quizAnswer.getQuestion() != null ? quizAnswer.getQuestion().getId() : null).questionText(quizAnswer.getQuestion() != null ? quizAnswer.getQuestion().getQuestionText() : null).questionType(quizAnswer.getQuestion() != null ? quizAnswer.getQuestion().getQuestionType() : null).questionScore(quizAnswer.getQuestion() != null ? quizAnswer.getQuestion().getScore() : null).earnedScore(quizAnswer.getEarnedScore()).isCorrect(quizAnswer.getIsCorrect()).userAnswer(quizAnswer.getUserAnswer());

        // Set options (với đầy đủ thông tin cho teacher)
        if (quizAnswer.getQuestion() != null && quizAnswer.getQuestion().getOptions() != null) {
            builder.options(mapOptions(quizAnswer.getQuestion().getOptions()));
        }

        // LUÔN set correctAnswer và explanation cho teacher
        if (quizAnswer.getQuestion() != null) {
            String correctAnswer = getCorrectAnswer(quizAnswer.getQuestion());
            builder.correctAnswer(correctAnswer);
            builder.explanation(quizAnswer.getQuestion().getExplanation());
        }

        return builder.build();
    }

    public static AnswerResultDTO toAnswerResultDTOForStudent(QuizAnswer quizAnswer) {
        if (quizAnswer == null) return null;

        AnswerResultDTO.AnswerResultDTOBuilder builder = AnswerResultDTO.builder().questionId(quizAnswer.getQuestion() != null ? quizAnswer.getQuestion().getId() : null).questionText(quizAnswer.getQuestion() != null ? quizAnswer.getQuestion().getQuestionText() : null).questionType(quizAnswer.getQuestion() != null ? quizAnswer.getQuestion().getQuestionType() : null).questionScore(quizAnswer.getQuestion() != null ? quizAnswer.getQuestion().getScore() : null).earnedScore(quizAnswer.getEarnedScore()).isCorrect(quizAnswer.getIsCorrect()).userAnswer(quizAnswer.getUserAnswer());

        // LUÔN set correctAnswer bất kể đúng hay sai
        if (quizAnswer.getQuestion() != null) {
            String correctAnswer = getCorrectAnswer(quizAnswer.getQuestion());
            builder.correctAnswer(correctAnswer);

            // LUÔN set explanation để học sinh học hỏi
            builder.explanation(quizAnswer.getQuestion().getExplanation());
        }

        // Set options (ẩn isCorrect cho student)
        if (quizAnswer.getQuestion() != null && quizAnswer.getQuestion().getOptions() != null) {
            builder.options(mapOptionsForStudent(quizAnswer.getQuestion().getOptions()));
        }

        // Set boolean answers for TRUE_FALSE questions (nếu có)
        if (quizAnswer.getQuestion() != null && quizAnswer.getQuestion().getQuestionType() == QuestionType.TRUE_FALSE) {
            try {
                String userAnswer = quizAnswer.getUserAnswer();
                if (userAnswer != null && !userAnswer.trim().isEmpty()) {
                    // Parse từ format: "Single Choice: 123" hoặc trực tiếp boolean
                    String cleanAnswer = userAnswer.replace("Single Choice:", "").trim();
                    builder.userBooleanAnswer(Boolean.parseBoolean(cleanAnswer));
                }
            } catch (Exception e) {
                // Ignore parsing errors
            }
        }

        return builder.build();
    }

    // ==================== HELPER METHODS ====================

    private static List<OptionDTO> mapOptions(List<Option> options) {
        if (options == null) return null;

        return options.stream().map(opt -> OptionDTO.builder().id(opt.getId()).optionText(opt.getOptionText()).isCorrect(opt.getIsCorrect()).orderIndex(opt.getOrderIndex()).imageUrl(opt.getImageUrl()).build()).collect(Collectors.toList());
    }

    private static List<OptionDTO> mapOptionsForStudent(List<Option> options) {
        if (options == null) return null;

        return options.stream().map(opt -> OptionDTO.builder().id(opt.getId()).optionText(opt.getOptionText())
                // ⭐ KHÔNG set isCorrect cho student ⭐
                .orderIndex(opt.getOrderIndex()).imageUrl(opt.getImageUrl()).build()).collect(Collectors.toList());
    }

    private static String getCorrectAnswer(korastudy.be.entity.Course.Question question) {
        if (question == null) return null;

        QuestionType type = question.getQuestionType();

        if (question.getOptions() != null && !question.getOptions().isEmpty()) {
            List<String> correctOptions = question.getOptions().stream().filter(opt -> opt != null && Boolean.TRUE.equals(opt.getIsCorrect())).map(Option::getOptionText).filter(text -> text != null && !text.trim().isEmpty()).collect(Collectors.toList());

            if (correctOptions.isEmpty()) {
                return null;
            }

            // Format theo loại câu hỏi
            switch (type) {
                case MULTIPLE_CHOICE:
                    if (correctOptions.size() == 1) {
                        return correctOptions.get(0);
                    } else {
                        // Format với bullet points cho dễ đọc
                        return correctOptions.stream().map(opt -> "• " + opt).collect(Collectors.joining("\n"));
                    }
                case FILL_IN_BLANK:
                    return "Các đáp án có thể chấp nhận: " + String.join(", ", correctOptions);
                case SINGLE_CHOICE:
                case TRUE_FALSE:
                default:
                    return correctOptions.get(0);
            }
        }

        return null;
    }
}