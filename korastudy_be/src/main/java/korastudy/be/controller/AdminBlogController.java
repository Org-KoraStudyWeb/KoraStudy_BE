package korastudy.be.controller;

import korastudy.be.dto.request.blog.CreatePostRequest;
import korastudy.be.dto.request.blog.UpdatePostRequest;
import korastudy.be.dto.request.blog.CategoryRequest;
import korastudy.be.dto.request.blog.PostFilterRequest;
import korastudy.be.dto.response.ApiSuccess;
import korastudy.be.dto.response.blog.*;
import korastudy.be.service.IAdminBlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/blog")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminBlogController {

    private final IAdminBlogService adminBlogService;

    /**
     * Lấy danh sách bài viết có phân trang, filter
     */
    @GetMapping("/posts")
    public ResponseEntity<Page<AdminPostResponse>> getAllPosts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<Long> categoryIds,
            @RequestParam(required = false) Boolean published,
            Pageable pageable) {
        Page<AdminPostResponse> posts = adminBlogService.getAllPosts(keyword, categoryIds, published, pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * Lấy chi tiết bài viết để edit
     */
    @GetMapping("/posts/{id}")
    public ResponseEntity<AdminPostResponse> getPostById(@PathVariable Long id) {
        AdminPostResponse post = adminBlogService.getPostById(id);
        return ResponseEntity.ok(post);
    }

    /**
     * Tạo bài viết mới (Admin)
     */
    @PostMapping("/posts")
    public ResponseEntity<AdminPostResponse> createPost(@RequestBody CreatePostRequest request) {
        AdminPostResponse post = adminBlogService.createPost(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }

    /**
     * Cập nhật bài viết
     */
    @PutMapping("/posts/{id}")
    public ResponseEntity<AdminPostResponse> updatePost(
            @PathVariable Long id,
            @RequestBody UpdatePostRequest request) {
        AdminPostResponse post = adminBlogService.updatePost(id, request);
        return ResponseEntity.ok(post);
    }

    /**
     * Xóa bài viết
     */
    @DeleteMapping("/posts/{id}")
    public ResponseEntity<ApiSuccess> deletePost(@PathVariable Long id) {
        adminBlogService.deletePost(id);
        return ResponseEntity.ok(ApiSuccess.of("Bài viết đã được xóa thành công"));
    }

    /**
     * Thay đổi trạng thái publish của bài viết
     */
    @PatchMapping("/posts/{id}/toggle-publish")
    public ResponseEntity<Map<String, Boolean>> togglePostPublish(@PathVariable Long id) {
        boolean isPublished = adminBlogService.togglePostPublish(id);
        return ResponseEntity.ok(Map.of("published", isPublished));
    }

    /**
     * Tạo mới category
     */
    @PostMapping("/categories")
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody CategoryRequest request) {
        CategoryResponse category = adminBlogService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }

    /**
     * Cập nhật category
     */
    @PutMapping("/categories/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @RequestBody CategoryRequest request) {
        CategoryResponse category = adminBlogService.updateCategory(id, request);
        return ResponseEntity.ok(category);
    }

    /**
     * Xóa category
     */
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<ApiSuccess> deleteCategory(@PathVariable Long id) {
        adminBlogService.deleteCategory(id);
        return ResponseEntity.ok(ApiSuccess.of("Danh mục đã được xóa thành công"));
    }

    /**
     * Lấy danh sách tất cả categories
     */
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> categories = adminBlogService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * Lấy category theo ID (optional)
     */
    @GetMapping("/categories/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        CategoryResponse category = adminBlogService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }
    /**
     * Lấy danh sách comments của bài viết (có phân trang)
     */
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<Page<AdminCommentResponse>> getPostComments(
            @PathVariable Long postId,
            Pageable pageable) {
        Page<AdminCommentResponse> comments = adminBlogService.getPostComments(postId, pageable);
        return ResponseEntity.ok(comments);
    }

    /**
     * Xóa comment
     */
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiSuccess> deleteComment(@PathVariable Long commentId) {
        adminBlogService.deleteComment(commentId);
        return ResponseEntity.ok(ApiSuccess.of("Bình luận đã được xóa thành công"));
    }

    /**
     * Thống kê blog
     */
    @GetMapping("/statistics")
    public ResponseEntity<PostStatisticsResponse> getBlogStatistics() {
        PostStatisticsResponse statistics = adminBlogService.getBlogStatistics();
        return ResponseEntity.ok(statistics);
    }
}