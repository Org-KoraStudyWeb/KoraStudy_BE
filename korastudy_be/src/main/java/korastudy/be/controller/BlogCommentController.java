package korastudy.be.controller;

import korastudy.be.dto.request.blog.PostCommentRequest;
import korastudy.be.dto.response.blog.PostCommentResponse;
import korastudy.be.payload.response.ApiError;
import korastudy.be.payload.response.ApiSuccess;
import korastudy.be.security.userprinciple.AccountDetailsImpl;
import korastudy.be.service.IBlogCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class BlogCommentController {

    private final IBlogCommentService commentService;

    /**
     * Lấy tất cả comment của 1 post
     */
    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<PostCommentResponse>> getAllComments(@PathVariable Long postId) {
        List<PostCommentResponse> comments = commentService.getAllComments(postId);
        return ResponseEntity.ok(comments);
    }

    /**
     * Tạo comment mới (cần đăng nhập)
     */
    @PostMapping("/{postId}/comments")
    public ResponseEntity<PostCommentResponse> addComment(
            @PathVariable Long postId,
            @RequestBody PostCommentRequest request,
            @AuthenticationPrincipal AccountDetailsImpl currentUser) {
        PostCommentResponse comment = commentService.addComment(postId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    /**
     * Update comment (chỉ user tạo comment hoặc admin)
     */
    @PutMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<?> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestBody PostCommentRequest request,
            @AuthenticationPrincipal AccountDetailsImpl currentUser) {
        try {
            PostCommentResponse comment = commentService.updateComment(postId, commentId, request, currentUser);
            return ResponseEntity.ok(comment);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiError.of(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiError.of(e.getMessage()));
        }
    }

    /**
     * Xoá comment (chỉ user tạo comment hoặc admin)
     */
    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal AccountDetailsImpl currentUser) {
        try {
            commentService.deleteComment(postId, commentId, currentUser);
            return ResponseEntity.ok(ApiSuccess.of("Comment deleted successfully"));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiError.of(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiError.of(e.getMessage()));
        }
    }
}