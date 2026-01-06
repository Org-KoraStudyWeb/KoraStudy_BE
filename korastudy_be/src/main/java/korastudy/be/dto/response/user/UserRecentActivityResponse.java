package korastudy.be.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRecentActivityResponse {
    private Long id;
    private String title;
    private ActivityType type;
    private LocalDateTime timestamp;
    private String score; // hiển thị điểm số (ví dụ: "85/100" hoặc "8.5")
    private String result; // kết quả (ví dụ: "Đạt", "Không đạt", hoăc null)

    public enum ActivityType {
        COMPREHENSIVE_TEST,
        PRACTICE_TEST,
        QUIZ
    }
}
