package korastudy.be.service.impl;

import korastudy.be.dto.request.blog.CreatePostRequest;
import korastudy.be.dto.request.blog.PostMetaRequest;
import korastudy.be.dto.request.blog.UpdatePostRequest;
import korastudy.be.dto.response.blog.PostMetaResponse;
import korastudy.be.dto.response.blog.PostResponse;
import korastudy.be.entity.Post.Category;
import korastudy.be.entity.Post.Post;
import korastudy.be.entity.Post.PostMeta;
import korastudy.be.entity.User.Account;
import korastudy.be.exception.ResourceNotFoundException;
import korastudy.be.repository.AccountRepository;
import korastudy.be.repository.blog.CategoryRepository;
import korastudy.be.repository.blog.PostMetaRepository;
import korastudy.be.repository.blog.PostRepository;
import korastudy.be.security.userprinciple.AccountDetailsImpl;
import korastudy.be.service.IPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService implements IPostService {

    private final PostRepository postRepository;
    private final PostMetaRepository postMetaRepository;
    private final CategoryRepository categoryRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PostResponse> getAllPosts() {
        return postRepository.findAll().stream()
                .map(PostResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PostResponse getPostById(Long id) {
        Post post = getPostEntityById(id);
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
        postRepository.delete(post);
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
        return postRepository.findById(id)
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
}