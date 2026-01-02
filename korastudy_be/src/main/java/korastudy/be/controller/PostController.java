package korastudy.be.controller;

import korastudy.be.dto.request.blog.CreatePostRequest;
import korastudy.be.dto.request.blog.PostMetaRequest;
import korastudy.be.dto.request.blog.UpdatePostRequest;
import korastudy.be.dto.response.blog.PostMetaResponse;
import korastudy.be.dto.response.blog.PostResponse;
import korastudy.be.payload.response.ApiError;
import korastudy.be.security.userprinciple.AccountDetailsImpl;
import korastudy.be.service.IPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final IPostService postService;

    /**
     * PUBLIC: Lấy tất cả bài viết
     */
    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts() {
        List<PostResponse> posts = postService.getAllPosts();
        return ResponseEntity.ok(posts);
    }

    /**
     * PUBLIC: Lấy chi tiết bài viết
     */
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable Long id) {
        PostResponse post = postService.getPostById(id);
        return ResponseEntity.ok(post);
    }

    /**
     * PUBLIC: Lấy metas của bài viết
     */
    @GetMapping("/{id}/meta")
    public ResponseEntity<List<PostMetaResponse>> getPostMeta(@PathVariable Long id) {
        List<PostMetaResponse> metas = postService.getPostMeta(id);
        return ResponseEntity.ok(metas);
    }

    /**
     * USER/ADMIN: Tạo bài viết mới kèm category
     */
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<PostResponse> createPost(
            @RequestBody CreatePostRequest request,
            @AuthenticationPrincipal AccountDetailsImpl currentUser) {
        PostResponse post = postService.createPost(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }

    /**
     * USER/ADMIN: Cập nhật bài viết
     */
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePost(
            @PathVariable Long id,
            @RequestBody UpdatePostRequest request,
            @AuthenticationPrincipal AccountDetailsImpl currentUser) {
        try {
            PostResponse post = postService.updatePost(id, request, currentUser);
            return ResponseEntity.ok(post);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiError.of(e.getMessage()));
        }
    }

    /**
     * ADMIN: Xóa bài viết
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * USER/ADMIN: Thêm meta cho bài viết
     */
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping("/{id}/meta")
    public ResponseEntity<?> addPostMeta(
            @PathVariable Long id,
            @RequestBody PostMetaRequest request,
            @AuthenticationPrincipal AccountDetailsImpl currentUser) {
        try {
            PostMetaResponse meta = postService.addPostMeta(id, request, currentUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(meta);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiError.of(e.getMessage()));
        }
    }

    /**
     * USER/ADMIN: Cập nhật meta của bài viết
     */
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PutMapping("/{postId}/meta/{metaId}")
    public ResponseEntity<?> updatePostMeta(
            @PathVariable Long postId,
            @PathVariable Long metaId,
            @RequestBody PostMetaRequest request,
            @AuthenticationPrincipal AccountDetailsImpl currentUser) {
        try {
            PostMetaResponse meta = postService.updatePostMeta(postId, metaId, request, currentUser);
            return ResponseEntity.ok(meta);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiError.of(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiError.of(e.getMessage()));
        }
    }

    /**
     * USER/ADMIN: Xóa meta của bài viết
     */
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @DeleteMapping("/{postId}/meta/{metaId}")
    public ResponseEntity<?> deletePostMeta(
            @PathVariable Long postId,
            @PathVariable Long metaId,
            @AuthenticationPrincipal AccountDetailsImpl currentUser) {
        try {
            postService.deletePostMeta(postId, metaId, currentUser);
            return ResponseEntity.noContent().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiError.of(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiError.of(e.getMessage()));
        }
    }

    /**
     * USER: Báo cáo bài viết
     */
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping("/{id}/report")
    public ResponseEntity<?> reportPost(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal AccountDetailsImpl currentUser) {
        try {
            String reason = request.get("reason");
            String description = request.get("description");
            postService.reportPost(id, currentUser.getId(), reason, description);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiError.of("Báo cáo đã được gửi thành công"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiError.of(e.getMessage()));
        }
    }
}
