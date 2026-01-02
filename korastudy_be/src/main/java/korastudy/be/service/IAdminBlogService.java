package korastudy.be.service;

import korastudy.be.dto.request.blog.CategoryRequest;
import korastudy.be.dto.request.blog.CreatePostRequest;
import korastudy.be.dto.request.blog.UpdatePostRequest;
import korastudy.be.dto.response.blog.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IAdminBlogService {
    
    // Quản lý bài viết
    Page<AdminPostResponse> getAllPosts(String keyword, List<Long> categoryIds, Boolean published, Pageable pageable);
    AdminPostResponse getPostById(Long id);
    AdminPostResponse createPost(CreatePostRequest request);
    AdminPostResponse updatePost(Long id, UpdatePostRequest request);
    void deletePost(Long id);
    boolean togglePostPublish(Long id);
    
    // Post status management
    AdminPostResponse approvePost(Long id);
    AdminPostResponse rejectPost(Long id, String reason);
    AdminPostResponse hidePost(Long id);
    String updateFeaturedImage(Long id, String imageUrl);
    
    // Report management
    List<PostReportResponse> getAllReports(String status);
    PostReportResponse getReportById(Long id);
    PostReportResponse reviewReport(Long id, String status, String adminNote);
    
    // Quản lý danh mục
    CategoryResponse createCategory(CategoryRequest request);
    CategoryResponse updateCategory(Long id, CategoryRequest request);
    void deleteCategory(Long id);
    List<CategoryResponse> getAllCategories();
    CategoryResponse getCategoryById(Long id);
    
    // Quản lý bình luận
    Page<AdminCommentResponse> getPostComments(Long postId, Pageable pageable);
    void deleteComment(Long commentId);
    
    // Thống kê
    PostStatisticsResponse getBlogStatistics();
}
