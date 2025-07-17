package korastudy.be.dto.response.blog;

import korastudy.be.entity.Post.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private Long id;
    private String name;
    private String description;
    private int postCount;

    public static CategoryResponse fromEntity(Category category) {
        return CategoryResponse.builder()
                .id(category.getId()) // Sử dụng getId
                .name(category.getName()) // Sử dụng getName
                .description(category.getDescription()) // Sử dụng getDescription
                .postCount(category.getPosts() != null ? category.getPosts().size() : 0)
                .build();
    }
}