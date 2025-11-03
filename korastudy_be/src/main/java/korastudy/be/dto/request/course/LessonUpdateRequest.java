package korastudy.be.dto.request.course;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import korastudy.be.entity.Enum.LessonType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LessonUpdateRequest {
    @NotBlank(message = "Tiêu đề bài học không được để trống")
    private String lessonTitle;
    
    @NotNull(message = "Loại nội dung không được để trống")
    private LessonType contentType;
    
    private String videoUrl;
    
    @NotBlank(message = "Nội dung bài học không được để trống")
    private String content;
    
    private Integer orderIndex;
    
    private Integer duration; // Thời lượng bài học tính bằng phút
}
