package korastudy.be.dto.response.course;

import korastudy.be.entity.Enum.LessonType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LessonDTO {
    private Long id;
    private String lessonTitle;
    private String content;
    private String videoUrl;
    private LessonType contentType;
    private Integer orderIndex;
    private Integer duration;
}
