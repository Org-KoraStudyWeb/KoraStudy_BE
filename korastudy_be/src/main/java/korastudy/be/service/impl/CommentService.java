package korastudy.be.service.impl;

import korastudy.be.dto.request.blog.PostCommentRequest;
import korastudy.be.dto.response.blog.PostCommentResponse;
import korastudy.be.entity.Post.Post;
import korastudy.be.entity.Post.PostComment;
import korastudy.be.entity.User.User;
import korastudy.be.exception.ResourceNotFoundException;
import korastudy.be.repository.UserRepository;
import korastudy.be.repository.blog.PostCommentRepository;
import korastudy.be.repository.blog.PostRepository;
import korastudy.be.security.userprinciple.AccountDetailsImpl;
import korastudy.be.service.ICommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService implements ICommentService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostCommentRepository postCommentRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PostCommentResponse> getAllComments(Long postId) {
        Post post = getPostById(postId);
        return post.getComments().stream()
                .map(PostCommentResponse::fromEntity)
                .toList();
    }

    @Override
    public PostCommentResponse addComment(Long postId, PostCommentRequest request, AccountDetailsImpl currentUser) {
        Post post = getPostById(postId);
        User user = getUserByAccountId(currentUser.getId());

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

    @Override
    public PostCommentResponse updateComment(Long postId, Long commentId, PostCommentRequest request, AccountDetailsImpl currentUser) {
        PostComment comment = getCommentById(commentId);

        // Validate comment belongs to post
        validateCommentBelongsToPost(comment, postId);

        // Check permissions
        validateUserPermission(comment, currentUser);

        comment.setContext(request.getContext());
        comment.setLastModified(LocalDateTime.now());
        postCommentRepository.save(comment);

        return PostCommentResponse.fromEntity(comment);
    }

    @Override
    public void deleteComment(Long postId, Long commentId, AccountDetailsImpl currentUser) {
        PostComment comment = getCommentById(commentId);

        // Validate comment belongs to post
        validateCommentBelongsToPost(comment, postId);

        // Check permissions
        validateUserPermission(comment, currentUser);

        postCommentRepository.delete(comment);
    }

    // Helper methods
    private Post getPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));
    }

    private User getUserByAccountId(Long accountId) {
        return userRepository.findByAccountId(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with account id: " + accountId));
    }

    private PostComment getCommentById(Long commentId) {
        return postCommentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));
    }

    private void validateCommentBelongsToPost(PostComment comment, Long postId) {
        if (!comment.getPost().getId().equals(postId)) {
            throw new IllegalArgumentException("Comment does not belong to this post");
        }
    }

    private void validateUserPermission(PostComment comment, AccountDetailsImpl currentUser) {
        User user = getUserByAccountId(currentUser.getId());

        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        boolean isCommentOwner = comment.getUser().getId().equals(user.getId());

        if (!isAdmin && !isCommentOwner) {
            throw new AccessDeniedException("Permission denied: You can only modify your own comments");
        }
    }
}