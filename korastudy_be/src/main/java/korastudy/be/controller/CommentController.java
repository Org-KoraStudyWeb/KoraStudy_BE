package korastudy.be.controller;

import korastudy.be.dto.request.blog.PostCommentRequest;
import korastudy.be.dto.response.blog.PostCommentResponse;
import korastudy.be.entity.Post.Post;
import korastudy.be.entity.Post.PostComment;
import korastudy.be.entity.User.User;
import korastudy.be.repository.blog.PostCommentRepository;
import korastudy.be.repository.blog.PostRepository;
import korastudy.be.repository.UserRepository;
import korastudy.be.security.userprinciple.AccountDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class CommentController {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostCommentRepository postCommentRepository;

    // ✅ Lấy tất cả comment của 1 post
    @GetMapping("/{postId}/comments")
    public List<PostCommentResponse> getAllComments(@PathVariable Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return post.getComments().stream()
                .map(PostCommentResponse::fromEntity)
                .toList();
    }

    // ✅ Tạo comment mới (cần đăng nhập)
    @PostMapping("/{postId}/comments")
    public PostCommentResponse addComment(
            @PathVariable Long postId,
            @RequestBody PostCommentRequest request,
            @AuthenticationPrincipal AccountDetailsImpl currentUser
    ) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        User user = userRepository.findByAccountId(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        PostComment comment = PostComment.builder()
                .context(request.getContext())
                .isPublished(true)
                .publishedAt(LocalDateTime.now())
                .post(post)
                .user(user)
                .build();

        postCommentRepository.save(comment);
        return PostCommentResponse.fromEntity(comment);
    }

    // ✅ Update comment (chỉ user tạo comment hoặc admin)
    @PutMapping("/{postId}/comments/{commentId}")
    public PostCommentResponse updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestBody PostCommentRequest request,
            @AuthenticationPrincipal AccountDetailsImpl currentUser
    ) {
        PostComment comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getPost().getId().equals(postId)) {
            throw new RuntimeException("Comment does not belong to this post");
        }

        // Check quyền (user comment hoặc admin)
        Long userId = userRepository.findByAccountId(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();

        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !comment.getUser().getId().equals(userId)) {
            throw new RuntimeException("Permission denied");
        }

        comment.setContext(request.getContext());
        comment.setLastModified(LocalDateTime.now());
        postCommentRepository.save(comment);
        return PostCommentResponse.fromEntity(comment);
    }

    // ✅ Xoá comment (chỉ user tạo comment hoặc admin)
    @DeleteMapping("/{postId}/comments/{commentId}")
    public void deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal AccountDetailsImpl currentUser
    ) {
        PostComment comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getPost().getId().equals(postId)) {
            throw new RuntimeException("Comment does not belong to this post");
        }

        Long userId = userRepository.findByAccountId(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();

        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !comment.getUser().getId().equals(userId)) {
            throw new RuntimeException("Permission denied");
        }

        postCommentRepository.delete(comment);
    }

}
