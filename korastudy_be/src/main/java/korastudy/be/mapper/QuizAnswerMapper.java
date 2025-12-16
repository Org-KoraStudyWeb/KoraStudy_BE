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

        AnswerResultDTO.AnswerResultDTOBuilder builder = AnswerResultDTO.builder()
                .questionId(quizAnswer.getQuestion() != null ? quizAnswer.getQuestion().getId() : null)
                .questionText(quizAnswer.getQuestion() != null ? quizAnswer.getQuestion().getQuestionText() : null)
                .questionType(quizAnswer.getQuestion() != null ? quizAnswer.getQuestion().getQuestionType() : null)
                .questionScore(quizAnswer.getQuestion() != null ? quizAnswer.getQuestion().getScore() : null)
                .earnedScore(quizAnswer.getEarnedScore())
                .isCorrect(quizAnswer.getIsCorrect())
                .userAnswer(quizAnswer.getUserAnswer());

        // Set options
        if (quizAnswer.getQuestion() != null && quizAnswer.getQuestion().getOptions() != null) {
            builder.options(mapOptions(quizAnswer.getQuestion().getOptions()));
        }

        // Set correct answer và explanation
        setCorrectAnswerAndExplanation(builder, quizAnswer);

        return builder.build();
    }

    public static AnswerResultDTO toAnswerResultDTOForStudent(QuizAnswer quizAnswer) {
        if (quizAnswer == null) return null;

        AnswerResultDTO.AnswerResultDTOBuilder builder = AnswerResultDTO.builder()
                .questionId(quizAnswer.getQuestion() != null ? quizAnswer.getQuestion().getId() : null)
                .questionText(quizAnswer.getQuestion() != null ? quizAnswer.getQuestion().getQuestionText() : null)
                .questionType(quizAnswer.getQuestion() != null ? quizAnswer.getQuestion().getQuestionType() : null)
                .questionScore(quizAnswer.getQuestion() != null ? quizAnswer.getQuestion().getScore() : null)
                .earnedScore(quizAnswer.getEarnedScore())
                .isCorrect(quizAnswer.getIsCorrect())
                .userAnswer(quizAnswer.getUserAnswer());

        // Set options (ẩn isCorrect cho student)
        if (quizAnswer.getQuestion() != null && quizAnswer.getQuestion().getOptions() != null) {
            builder.options(mapOptionsForStudent(quizAnswer.getQuestion().getOptions()));
        }

        // Student không xem được correctAnswer và explanation (trừ khi trả lời đúng)
        if (Boolean.TRUE.equals(quizAnswer.getIsCorrect())) {
            builder.explanation(quizAnswer.getQuestion() != null ? quizAnswer.getQuestion().getExplanation() : null);
        }

        // Set correct answer cho student khi câu hỏi đúng
        if (Boolean.TRUE.equals(quizAnswer.getIsCorrect())) {
            String correctAnswer = getCorrectAnswer(quizAnswer.getQuestion());
            builder.correctAnswer(correctAnswer);
        }

        return builder.build();
    }

    // ==================== HELPER METHODS ====================

    private static List<OptionDTO> mapOptions(List<Option> options) {
        if (options == null) return null;

        return options.stream()
                .map(opt -> OptionDTO.builder()
                        .id(opt.getId())
                        .optionText(opt.getOptionText())
                        .isCorrect(opt.getIsCorrect())
                        .orderIndex(opt.getOrderIndex())
                        .build())
                .collect(Collectors.toList());
    }

    private static List<OptionDTO> mapOptionsForStudent(List<Option> options) {
        if (options == null) return null;

        return options.stream()
                .map(opt -> OptionDTO.builder()
                        .id(opt.getId())
                        .optionText(opt.getOptionText())
                        // ⭐ KHÔNG set isCorrect cho student ⭐
                        .orderIndex(opt.getOrderIndex())
                        .build())
                .collect(Collectors.toList());
    }

    private static void setCorrectAnswerAndExplanation(AnswerResultDTO.AnswerResultDTOBuilder builder, QuizAnswer quizAnswer) {
        if (quizAnswer.getQuestion() == null) return;

        // Set explanation nếu trả lời đúng
        if (Boolean.TRUE.equals(quizAnswer.getIsCorrect())) {
            builder.explanation(quizAnswer.getQuestion().getExplanation());
        }

        // Set the correct answer
        String correctAnswer = getCorrectAnswer(quizAnswer.getQuestion());
        builder.correctAnswer(correctAnswer);

        // Set boolean answers for TRUE_FALSE questions
        if (quizAnswer.getQuestion().getQuestionType() == QuestionType.TRUE_FALSE) {
            try {
                String userAnswer = quizAnswer.getUserAnswer();
                if (userAnswer != null && !userAnswer.trim().isEmpty()) {
                    builder.userBooleanAnswer(Boolean.parseBoolean(userAnswer));
                }

                if (correctAnswer != null && !correctAnswer.trim().isEmpty()) {
                    builder.correctBooleanAnswer(Boolean.parseBoolean(correctAnswer));
                }
            } catch (Exception e) {
                // Ignore parsing errors
            }
        }
    }

    private static String getCorrectAnswer(korastudy.be.entity.Course.Question question) {
        if (question == null) return "";

        QuestionType type = question.getQuestionType();

        // For ALL question types, get the correct answers from options
        if (question.getOptions() != null && !question.getOptions().isEmpty()) {
            return question.getOptions().stream()
                    .filter(opt -> opt != null && Boolean.TRUE.equals(opt.getIsCorrect()))
                    .map(opt -> opt.getOptionText())
                    .collect(Collectors.joining(", "));
        }

        return "";
    }
}