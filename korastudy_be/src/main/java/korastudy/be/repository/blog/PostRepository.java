package korastudy.be.repository.blog;

import korastudy.be.entity.Post.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {
    long countByPublishedTrue();
    List<Post> findTop5ByOrderByViewCountDesc();

    // Soft delete helpers
    List<Post> findAllByDeletedAtIsNull();
    java.util.Optional<Post> findByIdAndDeletedAtIsNull(Long id);

    @Query("SELECT SUM(p.viewCount) FROM Post p")
    Long sumViewCount();

    @Query("SELECT FORMAT(p.createdAt, 'MM/yyyy') as monthYear, COUNT(p) " +
            "FROM Post p " +
            "WHERE p.createdAt >= :startDate " +
            "GROUP BY FORMAT(p.createdAt, 'MM/yyyy') " +
            "ORDER BY MIN(p.createdAt)")
    List<Object[]> countPostsByMonth(@Param("startDate") LocalDateTime startDate);

    default List<Object[]> countPostsByMonth() {
        return countPostsByMonth(LocalDateTime.now().minusYears(1));
    }
}
