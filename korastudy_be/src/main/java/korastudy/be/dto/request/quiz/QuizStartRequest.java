    package korastudy.be.dto.request.quiz;

    import lombok.AllArgsConstructor;
    import lombok.Builder;
    import lombok.Data;
    import lombok.NoArgsConstructor;

    /**
     * Request khi user BẮT ĐẦU làm quiz
     * Endpoint: POST /api/quizzes/{quizId}/start
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public class QuizStartRequest {
        // Có thể để trống nếu chỉ cần track thời gian bắt đầu
        // Hoặc thêm các field để validate:

        // Timezone của user (để tính thời gian chính xác)
        private String timezone;
    }
