package korastudy.be.repository.blog;

import korastudy.be.entity.Post.PostComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    //  Tìm tất cả bình luận theo postId
    List<PostComment> findByPostId(Long postId);

    // Tìm tất cả bình luận theo userId
    List<PostComment> findByUserId(Long userId);

    //  Lấy tất cả comment đang được public
    List<PostComment> findByIsPublishedTrue();

    Page<PostComment> findByPostId(Long postId, Pageable pageable);

    // Get only top-level comments (no parent) for a post
    List<PostComment> findByPostIdAndParentIsNull(Long postId);

    // Get replies for a given parent comment
    List<PostComment> findByParentId(Long parentId);
}

