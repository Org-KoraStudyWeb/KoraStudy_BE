package korastudy.be.service;

import korastudy.be.dto.request.blog.CategoryRequest;
import korastudy.be.dto.request.blog.CreatePostRequest;
import korastudy.be.dto.request.blog.UpdatePostRequest;
import korastudy.be.dto.response.blog.AdminCommentResponse;
import korastudy.be.dto.response.blog.AdminPostResponse;
import korastudy.be.dto.response.blog.CategoryResponse;
import korastudy.be.dto.response.blog.PostStatisticsResponse;
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
    
    // Quản lý danh mục
    CategoryResponse createCategory(CategoryRequest request);
    CategoryResponse updateCategory(Long id, CategoryRequest request);
    void deleteCategory(Long id);
    List<CategoryResponse> getAllCategories(); // Thêm method mới
    CategoryResponse getCategoryById(Long id); // Thêm method mới
    
    // Quản lý bình luận
    Page<AdminCommentResponse> getPostComments(Long postId, Pageable pageable);
    void deleteComment(Long commentId);
    
    // Thống kê
    PostStatisticsResponse getBlogStatistics();
}