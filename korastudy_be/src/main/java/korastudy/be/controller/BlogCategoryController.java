package korastudy.be.controller;

import korastudy.be.dto.request.blog.CategoryRequest;
import korastudy.be.dto.response.blog.CategoryResponse;
import korastudy.be.service.IBlogCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/categories")
@RequiredArgsConstructor
public class BlogCategoryController {

    private final IBlogCategoryService categoryService;

    /**
     * PUBLIC: Lấy tất cả category
     */
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * PUBLIC: Lấy chi tiết category
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        CategoryResponse category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    /**
     * ADMIN: Tạo mới category
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody CategoryRequest request) {
        CategoryResponse category = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }

    /**
     * ADMIN: Cập nhật category
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @RequestBody CategoryRequest request) {
        CategoryResponse category = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(category);
    }

    /**
     * ADMIN: Xoá category
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}