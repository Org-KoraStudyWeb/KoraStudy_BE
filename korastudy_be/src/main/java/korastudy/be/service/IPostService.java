package korastudy.be.service;

import korastudy.be.dto.request.blog.CreatePostRequest;
import korastudy.be.dto.request.blog.PostMetaRequest;
import korastudy.be.dto.request.blog.UpdatePostRequest;
import korastudy.be.dto.response.blog.PostMetaResponse;
import korastudy.be.dto.response.blog.PostResponse;
import korastudy.be.security.userprinciple.AccountDetailsImpl;

import java.util.List;

public interface IPostService {

    // Public endpoints
    List<PostResponse> getAllPosts();
    PostResponse getPostById(Long id);
    List<PostMetaResponse> getPostMeta(Long id);

    // User/Admin endpoints
    PostResponse createPost(CreatePostRequest request, AccountDetailsImpl currentUser);
    PostResponse updatePost(Long id, UpdatePostRequest request, AccountDetailsImpl currentUser);
    PostMetaResponse addPostMeta(Long id, PostMetaRequest request, AccountDetailsImpl currentUser);
    PostMetaResponse updatePostMeta(Long postId, Long metaId, PostMetaRequest request, AccountDetailsImpl currentUser);
    void deletePostMeta(Long postId, Long metaId, AccountDetailsImpl currentUser);
    
    // Report post
    void reportPost(Long postId, Long userId, String reason, String description);

    // Admin only endpoints
    void deletePost(Long id);
}
