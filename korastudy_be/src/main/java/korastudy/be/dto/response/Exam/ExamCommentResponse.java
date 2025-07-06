package korastudy.be.dto.response.Exam;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamCommentResponse {
    private Long id;
    private String context;
    private String username;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
