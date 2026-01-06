package korastudy.be.service.impl;

import korastudy.be.dto.request.blog.CreatePostRequest;
import korastudy.be.dto.request.blog.PostMetaRequest;
import korastudy.be.dto.request.blog.UpdatePostRequest;
import korastudy.be.dto.response.blog.PostMetaResponse;
import korastudy.be.dto.response.blog.PostResponse;
import korastudy.be.entity.Enum.PostStatus;
import korastudy.be.entity.Enum.ReportStatus;
import korastudy.be.entity.Post.Category;
import korastudy.be.entity.Post.Post;
import korastudy.be.entity.Post.PostMeta;
import korastudy.be.entity.Post.PostReport;
import korastudy.be.entity.User.Account;
import korastudy.be.entity.User.User;
import korastudy.be.exception.ResourceNotFoundException;
import korastudy.be.repository.AccountRepository;
import korastudy.be.repository.UserRepository;
import korastudy.be.repository.blog.CategoryRepository;
import korastudy.be.repository.blog.PostMetaRepository;
import korastudy.be.repository.blog.PostReportRepository;
import korastudy.be.repository.blog.PostRepository;
import korastudy.be.security.userprinciple.AccountDetailsImpl;
import korastudy.be.service.IPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService implements IPostService {

    private final PostRepository postRepository;
    private final PostMetaRepository postMetaRepository;
    private final CategoryRepository categoryRepository;
    private final AccountRepository accountRepository;
    private final PostReportRepository postReportRepository;
    private final UserRepository userRepository;
    private final korastudy.be.service.INotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public List<PostResponse> getAllPosts(Long categoryId) {
        List<Post> posts;
        
        if (categoryId != null) {
            posts = postRepository.findAllByCategories_IdAndDeletedAtIsNull(categoryId);
        } else {
            posts = postRepository.findAllByDeletedAtIsNull();
        }

        return posts.stream()
                .filter(post -> post.getPostStatus() == null || post.getPostStatus() == PostStatus.APPROVED)
                .filter(post -> Boolean.TRUE.equals(post.getPublished()))
                .map(PostResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional // Removed readOnly = true to allow update
    public PostResponse getPostById(Long id) {
        Post post = getPostEntityById(id);
        
        // Increment view count
        post.setViewCount((post.getViewCount() == null ? 0 : post.getViewCount()) + 1);
        postRepository.save(post);
        
        return PostResponse.fromEntity(post);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostMetaResponse> getPostMeta(Long id) {
        Post post = getPostEntityById(id);
        return post.getMetas().stream()
                .map(PostMetaResponse::fromEntity)
                .toList();
    }

    @Override
    public PostResponse createPost(CreatePostRequest request, AccountDetailsImpl currentUser) {
        Account author = getAccountById(currentUser.getId());

        Post post = Post.builder()
                .postTitle(request.getPostTitle())
                .postSummary(request.getPostSummary())
                .postContent(request.getPostContent())
                .published(request.getPostPublished())
                .featuredImage(request.getFeaturedImage())
                .postStatus(PostStatus.PENDING)
                .createdBy(author)
                .build();

        // Gán categories nếu có
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(request.getCategoryIds());
            categories.forEach(post::addCategory);
        }

        // Gán metas nếu có
        if (request.getPostMetas() != null) {
            request.getPostMetas().forEach(metaReq -> {
                PostMeta meta = PostMeta.builder()
                        .metaKey(metaReq.getMetaKey())
                        .postMetaContext(metaReq.getPostMetaContext())
                        .build();
                post.addMeta(meta);
            });
        }

        postRepository.save(post);
        return PostResponse.fromEntity(post);
    }

    @Override
    public PostResponse updatePost(Long id, UpdatePostRequest request, AccountDetailsImpl currentUser) {
        Post post = getPostEntityById(id);
        Account currentAccount = getAccountById(currentUser.getId());

        // Check permissions
        validatePostUpdatePermission(post, currentUser, currentAccount);

        // Update post fields
        post.setPostTitle(request.getPostTitle());
        post.setPostSummary(request.getPostSummary());
        post.setPostContent(request.getPostContent());
        post.setPublished(request.getPostPublished());
        post.setFeaturedImage(request.getFeaturedImage());
        // Bỏ post.setLastModified(LocalDateTime.now())
        // BaseTimeEntity sẽ tự động update thông qua JPA auditing

        // Update metas
        post.getMetas().clear();
        if (request.getPostMetas() != null) {
            request.getPostMetas().forEach(metaReq -> {
                PostMeta meta = PostMeta.builder()
                        .metaKey(metaReq.getMetaKey())
                        .postMetaContext(metaReq.getPostMetaContext())
                        .build();
                post.addMeta(meta);
            });
        }

        postRepository.save(post);
        return PostResponse.fromEntity(post);
    }

    @Override
    public void deletePost(Long id) {
    Post post = getPostEntityById(id);
    // Soft delete: mark deleted timestamp instead of physical removal
    post.setDeletedAt(java.time.LocalDateTime.now());
    // No need to remove relations thanks to orphanRemoval; keep history
    postRepository.save(post);
    }

    @Override
    public PostMetaResponse addPostMeta(Long id, PostMetaRequest request, AccountDetailsImpl currentUser) {
        Post post = getPostEntityById(id);
        Account currentAccount = getAccountById(currentUser.getId());

        // Check permissions
        validatePostUpdatePermission(post, currentUser, currentAccount);

        PostMeta meta = PostMeta.builder()
                .metaKey(request.getMetaKey())
                .postMetaContext(request.getPostMetaContext())
                .build();

        post.addMeta(meta);
        postMetaRepository.save(meta);
        return PostMetaResponse.fromEntity(meta);
    }

    @Override
    public PostMetaResponse updatePostMeta(Long postId, Long metaId, PostMetaRequest request, AccountDetailsImpl currentUser) {
        PostMeta meta = getPostMetaById(metaId);
        Account currentAccount = getAccountById(currentUser.getId());

        // Validate meta belongs to post
        validateMetaBelongsToPost(meta, postId);

        // Check permissions
        validatePostUpdatePermission(meta.getPost(), currentUser, currentAccount);

        meta.setMetaKey(request.getMetaKey());
        meta.setPostMetaContext(request.getPostMetaContext());
        postMetaRepository.save(meta);
        return PostMetaResponse.fromEntity(meta);
    }

    @Override
    public void deletePostMeta(Long postId, Long metaId, AccountDetailsImpl currentUser) {
        PostMeta meta = getPostMetaById(metaId);
        Account currentAccount = getAccountById(currentUser.getId());

        // Validate meta belongs to post
        validateMetaBelongsToPost(meta, postId);

        // Check permissions
        validatePostUpdatePermission(meta.getPost(), currentUser, currentAccount);

        postMetaRepository.delete(meta);
    }

    // Helper methods
    private Post getPostEntityById(Long id) {
    return postRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));
    }

    private Account getAccountById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));
    }

    private PostMeta getPostMetaById(Long id) {
        return postMetaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PostMeta not found with id: " + id));
    }

    private void validateMetaBelongsToPost(PostMeta meta, Long postId) {
        if (!meta.getPost().getId().equals(postId)) {
            throw new IllegalArgumentException("Meta does not belong to this post");
        }
    }

    private void validatePostUpdatePermission(Post post, AccountDetailsImpl currentUser, Account currentAccount) {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        boolean isOwner = post.getCreatedBy().getId().equals(currentAccount.getId());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("You do not have permission to modify this post");
        }
    }

    @Override
    public void reportPost(Long postId, Long userId, String reason, String description) {
        Post post = getPostEntityById(postId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        // Check if user already reported this post
        Optional<PostReport> existing = postReportRepository
            .findByPostIdAndReporterId(postId, userId);
        
        if (existing.isPresent()) {
            throw new IllegalStateException("Bạn đã báo cáo bài viết này rồi");
        }
        
        PostReport report = PostReport.builder()
            .post(post)
            .reporter(user)
            .reason(reason)
            .description(description)
            .status(ReportStatus.PENDING)
            .build();
        
        PostReport savedReport = postReportRepository.save(report);
        
        // Gửi thông báo cho admin
        String reporterName = user.getFirstName() + " " + user.getLastName();
        String postTitle = post.getPostTitle();
        notificationService.sendBlogReportNotificationToAdmins(
            reporterName, 
            postTitle, 
            post.getId(), 
            savedReport.getId(), 
            reason
        );
    }
}