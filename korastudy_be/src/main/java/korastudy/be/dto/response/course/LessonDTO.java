package korastudy.be.dto.response.course;

import korastudy.be.dto.response.quiz.QuizDTO;
import korastudy.be.entity.Enum.LessonType;
import lombok.Data;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@Builder
public class LessonDTO {
    private Long id;
    private String lessonTitle;
    private String content;
    private String videoUrl;
    private String documentUrl;
    private LessonType contentType;
    private Integer orderIndex;
    private Integer duration; // phút
    private Long sectionId;
    private String sectionName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}