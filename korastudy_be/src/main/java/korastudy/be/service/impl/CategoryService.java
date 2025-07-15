package korastudy.be.service.impl;

import korastudy.be.dto.request.blog.CategoryRequest;
import korastudy.be.dto.response.blog.CategoryResponse;
import korastudy.be.entity.Post.Category;
import korastudy.be.exception.ResourceNotFoundException;
import korastudy.be.repository.blog.CategoryRepository;
import korastudy.be.service.ICategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService implements ICategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(CategoryResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = getCategoryEntityById(id);
        return CategoryResponse.fromEntity(category);
    }

    @Override
    public CategoryResponse createCategory(CategoryRequest request) {
        Category category = Category.builder()
                .categoryTitle(request.getCategoryTitle())
                .context(request.getContext())
                .build();
        // BaseTimeEntity sẽ tự động set createdAt và lastModified

        Category saved = categoryRepository.save(category);
        return CategoryResponse.fromEntity(saved);
    }

    @Override
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = getCategoryEntityById(id);

        category.setCategoryTitle(request.getCategoryTitle());
        category.setContext(request.getContext());
        // BaseTimeEntity sẽ tự động update lastModified

        categoryRepository.save(category);
        return CategoryResponse.fromEntity(category);
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = getCategoryEntityById(id);
        categoryRepository.delete(category);
    }

    // Helper methods
    private Category getCategoryEntityById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }
}