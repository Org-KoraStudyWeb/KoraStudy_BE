package korastudy.be.repository.blog;

import korastudy.be.entity.Post.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    // ✅ Tìm tất cả bình luận theo postId
    List<PostComment> findByPostId(Long postId);

    // ✅ (Tùy chọn) Tìm tất cả bình luận theo userId
    List<PostComment> findByUserId(Long userId);

    // ✅ (Tùy chọn) Lấy tất cả comment đang được public
    List<PostComment> findByIsPublishedTrue();

    // Bạn cũng có thể thêm các phương thức custom query nếu muốn
}

