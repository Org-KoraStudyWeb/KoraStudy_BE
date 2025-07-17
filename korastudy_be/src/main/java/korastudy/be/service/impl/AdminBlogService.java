package korastudy.be.service.impl;

import korastudy.be.dto.request.blog.CategoryRequest;
import korastudy.be.dto.request.blog.CreatePostRequest;
import korastudy.be.dto.request.blog.UpdatePostRequest;
import korastudy.be.dto.response.blog.*;
import korastudy.be.entity.Post.Category;
import korastudy.be.entity.Post.Post;
import korastudy.be.entity.Post.PostComment;
import korastudy.be.entity.Post.PostMeta;
import korastudy.be.entity.User.Account;
import korastudy.be.exception.ResourceNotFoundException;
import korastudy.be.repository.AccountRepository;
import korastudy.be.repository.blog.CategoryRepository;
import korastudy.be.repository.blog.PostCommentRepository;
import korastudy.be.repository.blog.PostRepository;
import korastudy.be.security.userprinciple.AccountDetailsImpl;
import korastudy.be.service.IAdminBlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminBlogService implements IAdminBlogService {

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final PostCommentRepository commentRepository;
    private final AccountRepository accountRepository;

    @Override
    public Page<AdminPostResponse> getAllPosts(String keyword, List<Long> categoryIds, Boolean published, Pageable pageable) {
        Specification<Post> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by keyword
            if (keyword != null && !keyword.isEmpty()) {
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("postTitle")), "%" + keyword.toLowerCase() + "%"),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("postSummary")), "%" + keyword.toLowerCase() + "%"),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("postContent")), "%" + keyword.toLowerCase() + "%")
                ));
            }

            // Filter by category
            if (categoryIds != null && !categoryIds.isEmpty()) {
                predicates.add(root.join("categories").get("id").in(categoryIds));
            }

            // Filter by publish status
            if (published != null) {
                predicates.add(criteriaBuilder.equal(root.get("published"), published));
            }

            // Chỉ lấy các bài viết chưa bị xóa
            predicates.add(criteriaBuilder.isNull(root.get("deletedAt")));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<Post> postPage = postRepository.findAll(spec, pageable);

        List<AdminPostResponse> postResponses = postPage.getContent().stream()
                .map(AdminPostResponse::fromEntity)
                .collect(Collectors.toList());

        return new PageImpl<>(postResponses, pageable, postPage.getTotalElements());
    }

    @Override
    public AdminPostResponse getPostById(Long id) {
        Post post = getPostEntityById(id);
        return AdminPostResponse.fromEntity(post);
    }

    @Override
    public AdminPostResponse createPost(CreatePostRequest request) {
        AccountDetailsImpl currentUser = (AccountDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Account author = accountRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        Post post = Post.builder()
                .postTitle(request.getPostTitle())
                .postSummary(request.getPostSummary())
                .postContent(request.getPostContent())
                .published(request.getPostPublished())
                .createdBy(author)
                .viewCount(0)
                .build();
        
        // Khởi tạo danh sách
        post.setMetas(new ArrayList<>());
        post.setCategories(new ArrayList<>());
        post.setTags(new ArrayList<>());
        post.setComments(new ArrayList<>());

        if (Boolean.TRUE.equals(request.getPostPublished())) {
            post.setPublishedAt(LocalDateTime.now());
        }

        // Add categories
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(request.getCategoryIds());
            categories.forEach(post::addCategory);
        }

        // Add metas
        if (request.getPostMetas() != null && !request.getPostMetas().isEmpty()) {
            request.getPostMetas().forEach(metaReq -> {
                PostMeta meta = PostMeta.builder()
                        .metaKey(metaReq.getMetaKey())
                        .postMetaContext(metaReq.getPostMetaContext())
                        .build();
                post.addMeta(meta);
            });
        }

        Post savedPost = postRepository.save(post);
        return AdminPostResponse.fromEntity(savedPost);
    }

    @Override
    public AdminPostResponse updatePost(Long id, UpdatePostRequest request) {
        Post post = getPostEntityById(id);

        post.setPostTitle(request.getPostTitle());
        post.setPostSummary(request.getPostSummary());
        post.setPostContent(request.getPostContent());

        // Xử lý thay đổi trạng thái published
        if (Boolean.TRUE.equals(request.getPostPublished()) && !Boolean.TRUE.equals(post.getPublished())) {
            // Chuyển từ draft sang published
            post.setPublished(true);
            post.setPublishedAt(LocalDateTime.now());
        } else if (Boolean.FALSE.equals(request.getPostPublished()) && Boolean.TRUE.equals(post.getPublished())) {
            // Chuyển từ published sang draft
            post.setPublished(false);
        }

        // Cập nhật metas
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

        Post updatedPost = postRepository.save(post);
        return AdminPostResponse.fromEntity(updatedPost);
    }

    @Override
    public void deletePost(Long id) {
        Post post = getPostEntityById(id);
        // Soft delete - chỉ đánh dấu thời gian xóa
        post.setDeletedAt(LocalDateTime.now());
        postRepository.save(post);
    }

    @Override
    public boolean togglePostPublish(Long id) {
        Post post = getPostEntityById(id);
        boolean newStatus = !Boolean.TRUE.equals(post.getPublished());
        post.setPublished(newStatus);

        if (newStatus) {
            post.setPublishedAt(LocalDateTime.now());
        }

        postRepository.save(post);
        return newStatus;
    }

    @Override
    public CategoryResponse createCategory(CategoryRequest request) {
        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        Category savedCategory = categoryRepository.save(category);
        return CategoryResponse.fromEntity(savedCategory);
    }

    @Override
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = getCategoryEntityById(id); // Sử dụng helper method

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        Category updatedCategory = categoryRepository.save(category);
        return CategoryResponse.fromEntity(updatedCategory);
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = getCategoryEntityById(id); // Sử dụng helper method

        // Kiểm tra xem category có đang được sử dụng không
        if (!category.getPosts().isEmpty()) {
            throw new IllegalArgumentException("Cannot delete category as it is associated with posts");
        }

        categoryRepository.delete(category);
    }

    // ===== THÊM CÁC METHOD MỚI CHO CATEGORY =====

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(CategoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = getCategoryEntityById(id);
        return CategoryResponse.fromEntity(category);
    }

    @Override
    public Page<AdminCommentResponse> getPostComments(Long postId, Pageable pageable) {
        Post post = getPostEntityById(postId);
        Page<PostComment> commentPage = commentRepository.findByPostId(postId, pageable);

        List<AdminCommentResponse> commentResponses = commentPage.getContent().stream()
                .map(AdminCommentResponse::fromEntity)
                .collect(Collectors.toList());

        return new PageImpl<>(commentResponses, pageable, commentPage.getTotalElements());
    }

    @Override
    public void deleteComment(Long commentId) {
        PostComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

        commentRepository.delete(comment);
    }

    @Override
    public PostStatisticsResponse getBlogStatistics() {
        // Tổng số bài viết
        long totalPosts = postRepository.count();

        // Số bài viết đã xuất bản và draft
        long publishedPosts = postRepository.countByPublishedTrue();
        long draftPosts = totalPosts - publishedPosts;

        // Tổng số bình luận
        long totalComments = commentRepository.count();

        // Tổng số danh mục
        long totalCategories = categoryRepository.count();

        // Tổng lượt xem
        Long totalViews = postRepository.sumViewCount();

        // Top danh mục có nhiều bài viết nhất
        List<Object[]> topCategoriesData = categoryRepository.findTopCategoriesByPostCount(5);
        List<PostStatisticsResponse.CategoryStatsDTO> topCategories = topCategoriesData.stream()
                .map(data -> PostStatisticsResponse.CategoryStatsDTO.builder()
                        .id((Long) data[0])
                        .name((String) data[1])
                        .postCount((Long) data[2])
                        .build())
                .collect(Collectors.toList());

        // Top bài viết có nhiều lượt xem nhất
        List<Post> mostViewedPosts = postRepository.findTop5ByOrderByViewCountDesc();
        List<PostStatisticsResponse.PostViewDTO> mostViewedPostsDTO = mostViewedPosts.stream()
                .map(post -> PostStatisticsResponse.PostViewDTO.builder()
                        .id(post.getId())
                        .title(post.getPostTitle())
                        .viewCount(post.getViewCount())
                        .build())
                .collect(Collectors.toList());

        // Thống kê bài viết theo tháng
        List<Object[]> postsByMonthData = postRepository.countPostsByMonth();
        Map<String, Long> postsByMonth = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");

        // Lấy 12 tháng gần nhất
        LocalDateTime now = LocalDateTime.now();
        for (int i = 11; i >= 0; i--) {
            LocalDateTime monthDate = now.minusMonths(i);
            String monthKey = monthDate.format(formatter);
            postsByMonth.put(monthKey, 0L);
        }

        // Điền dữ liệu thực tế
        for (Object[] data : postsByMonthData) {
            String monthYear = (String) data[0];
            Long count = (Long) data[1];
            postsByMonth.put(monthYear, count);
        }

        return PostStatisticsResponse.builder()
                .totalPosts(totalPosts)
                .publishedPosts(publishedPosts)
                .draftPosts(draftPosts)
                .totalComments(totalComments)
                .totalCategories(totalCategories)
                .totalViews(totalViews != null ? totalViews : 0L)
                .topCategories(topCategories)
                .mostViewedPosts(mostViewedPostsDTO)
                .postsByMonth(postsByMonth)
                .build();
    }

    // ===== HELPER METHODS =====

    private Post getPostEntityById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));
    }

    private Category getCategoryEntityById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }
}