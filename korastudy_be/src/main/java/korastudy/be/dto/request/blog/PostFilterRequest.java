package korastudy.be.dto.request.blog;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostFilterRequest {
    private String keyword;
    private List<Long> categoryIds;
    private Boolean published;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}