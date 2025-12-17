package korastudy.be.dto.request.quiz;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QuizSearchRequest {
    private String title;
    private Long sectionId;

    private Boolean isPublished;    // Lọc theo trạng thái
    private Boolean isActive;       // Lọc theo active
    private LocalDateTime fromDate; // Tìm từ ngày
    private LocalDateTime toDate;   // Tìm đến ngày

    @Min(value = 0, message = "Page không được âm")
    private Integer page = 0;

    @Min(value = 1, message = "Size phải lớn hơn 0")
    @Max(value = 100, message = "Size không được vượt quá 100")
    private Integer size = 10;
}