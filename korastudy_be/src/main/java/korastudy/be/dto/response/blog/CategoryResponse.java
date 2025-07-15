package korastudy.be.dto.response.blog;

import korastudy.be.entity.Post.Category;
import lombok.Data;

@Data
public class CategoryResponse {
    private Long categoryId;
    private String categoryTitle;
    private String context;

    public static CategoryResponse fromEntity(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setCategoryId(category.getCategoryId());
        response.setCategoryTitle(category.getCategoryTitle());
        response.setContext(category.getContext());
        return response;
    }
}

