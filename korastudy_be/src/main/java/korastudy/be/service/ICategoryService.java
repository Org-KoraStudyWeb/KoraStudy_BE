package korastudy.be.service;

import korastudy.be.dto.request.blog.CategoryRequest;
import korastudy.be.dto.response.blog.CategoryResponse;

import java.util.List;

public interface ICategoryService {

    // Public endpoints
    List<CategoryResponse> getAllCategories();
    CategoryResponse getCategoryById(Long id);

    // Admin endpoints
    CategoryResponse createCategory(CategoryRequest request);
    CategoryResponse updateCategory(Long id, CategoryRequest request);
    void deleteCategory(Long id);
}