package korastudy.be.controller;

import korastudy.be.dto.request.blog.*;
import korastudy.be.dto.response.blog.PostMetaResponse;
import korastudy.be.dto.response.blog.PostResponse;
import korastudy.be.entity.Post.Post;
import korastudy.be.entity.Post.PostMeta;
import korastudy.be.entity.User.Account;
import korastudy.be.repository.AccountRepository;
import korastudy.be.repository.blog.PostMetaRepository;
import korastudy.be.repository.blog.PostRepository;
import korastudy.be.security.userprinciple.AccountDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostRepository postRepository;
    private final PostMetaRepository postMetaRepository;
    private final AccountRepository accountRepository;

    //  PUBLIC: Lấy tất cả bài viết
    @GetMapping
    public List<PostResponse> getAllPosts() {
        return postRepository.findAll().stream()
                .map(PostResponse::fromEntity)
                .toList();
    }

    //  PUBLIC: Lấy chi tiết bài viết
    @GetMapping("/{id}")
    public PostResponse getPostById(@PathVariable Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return PostResponse.fromEntity(post);
    }

    //  PUBLIC: Lấy metas
    @GetMapping("/{id}/meta")
    public List<PostMetaResponse> getPostMeta(@PathVariable Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return post.getMetas().stream()
                .map(PostMetaResponse::fromEntity)
                .toList();
    }

    //  USER/ADMIN: Tạo bài viết mới
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping
    public PostResponse createPost(@RequestBody CreatePostRequest request) {
        Post post = new Post();
        post.setPostTitle(request.getPostTitle());
        post.setPostSummary(request.getPostSummary());
        post.setPostContent(request.getPostContent());
        post.setPublished(request.getPostPublished());
        post.setCreatedAt(LocalDateTime.now());
        post.setLastModified(LocalDateTime.now());

        // 🟢 Gán người tạo - LẤY ID TỪ TOKEN ĐANG ĐĂNG NHẬP
        Long currentUserId = getCurrentUserId();
        Account author = accountRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        post.setCreatedBy(author);

        // Xử lý metas nếu có
        if (request.getPostMetas() != null) {
            List<PostMeta> metas = request.getPostMetas().stream().map(metaReq -> {
                PostMeta meta = new PostMeta();
                meta.setMetaKey(metaReq.getMetaKey());
                meta.setPostMetaContext(metaReq.getPostMetaContext());
                meta.setPost(post);
                return meta;
            }).toList();
            post.setMetas(metas);
        }

        postRepository.save(post);
        return PostResponse.fromEntity(post);
    }


    //  USER/ADMIN: Cập nhật bài viết của chính mình
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PutMapping("/{id}")
    public PostResponse updatePost(@PathVariable Long id, @RequestBody UpdatePostRequest request) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Long currentUserId = getCurrentUserId();
        boolean isOwner = post.getCreatedBy().getId().equals(currentUserId);

        // Nếu không phải admin thì chỉ được cập nhật bài của mình
        if (!isCurrentUserAdmin() && !isOwner) {
            throw new RuntimeException("You do not have permission to update this post");
        }

        post.setPostTitle(request.getPostTitle());
        post.setPostSummary(request.getPostSummary());
        post.setPostContent(request.getPostContent());
        post.setPublished(request.getPostPublished());
        post.setLastModified(LocalDateTime.now());

        post.getMetas().clear();
        if (request.getPostMetas() != null) {
            request.getPostMetas().forEach(metaReq -> {
                PostMeta meta = new PostMeta();
                meta.setMetaKey(metaReq.getMetaKey());
                meta.setPostMetaContext(metaReq.getPostMetaContext());
                meta.setPost(post);
                post.getMetas().add(meta);
            });
        }

        postRepository.save(post);
        return PostResponse.fromEntity(post);
    }

    //  ADMIN: Xóa bài
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deletePost(@PathVariable Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        postRepository.delete(post);
    }

    //  USER/ADMIN: Thêm 1 meta
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping("/{id}/meta")
    public PostMetaResponse addPostMeta(@PathVariable Long id, @RequestBody PostMetaRequest request) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        PostMeta meta = new PostMeta();
        meta.setMetaKey(request.getMetaKey());
        meta.setPostMetaContext(request.getPostMetaContext());
        meta.setPost(post);
        postMetaRepository.save(meta);
        return PostMetaResponse.fromEntity(meta);
    }

    //  USER/ADMIN: Cập nhật 1 meta
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PutMapping("/{postId}/meta/{metaId}")
    public PostMetaResponse updatePostMeta(
            @PathVariable Long postId,
            @PathVariable Long metaId,
            @RequestBody PostMetaRequest request
    ) {
        PostMeta meta = postMetaRepository.findById(metaId)
                .orElseThrow(() -> new RuntimeException("Meta not found"));

        if (!meta.getPost().getId().equals(postId)) {
            throw new RuntimeException("Meta does not belong to this post");
        }

        meta.setMetaKey(request.getMetaKey());
        meta.setPostMetaContext(request.getPostMetaContext());
        postMetaRepository.save(meta);
        return PostMetaResponse.fromEntity(meta);
    }

    //  USER/ADMIN: Xóa 1 meta
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @DeleteMapping("/{postId}/meta/{metaId}")
    public void deletePostMeta(@PathVariable Long postId, @PathVariable Long metaId) {
        PostMeta meta = postMetaRepository.findById(metaId)
                .orElseThrow(() -> new RuntimeException("Meta not found"));

        if (!meta.getPost().getId().equals(postId)) {
            throw new RuntimeException("Meta does not belong to this post");
        }

        postMetaRepository.delete(meta);
    }


    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AccountDetailsImpl userDetails = (AccountDetailsImpl) authentication.getPrincipal();
        return userDetails.getId();
    }


    private boolean isCurrentUserAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }
}
