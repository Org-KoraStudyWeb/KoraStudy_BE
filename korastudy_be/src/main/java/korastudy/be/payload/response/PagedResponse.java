package korastudy.be.payload.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PagedResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    
    public boolean isLast() {
        return page == totalPages - 1;
    }
    
    public boolean isFirst() {
        return page == 0;
    }
}
