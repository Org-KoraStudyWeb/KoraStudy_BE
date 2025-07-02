package korastudy.be.dto.response;

import korastudy.be.dto.exam.MockTestDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamListResponse {
    private List<MockTestDTO> content;
    private int totalPages;
    private long totalElements;
    private int currentPage;
    private int size;
    private boolean first;
    private boolean last;
    private boolean hasNext;
    private boolean hasPrevious;
}
