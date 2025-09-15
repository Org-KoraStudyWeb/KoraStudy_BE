package korastudy.be.dto.response.course;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReviewDTO {
    private Long id;
    private Long userId;
    private String username;
    private String userAvatar;
    private Long courseId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
