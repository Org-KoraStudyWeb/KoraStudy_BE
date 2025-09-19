package korastudy.be.repository.blog;

import korastudy.be.entity.Post.Category;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Query("SELECT c.id, c.name, COUNT(p) as postCount " +
            "FROM Category c LEFT JOIN c.posts p " +
            "GROUP BY c.id, c.name " +
            "ORDER BY postCount DESC")
    List<Object[]> findTopCategoriesByPostCount(Pageable pageable);

    default List<Object[]> findTopCategoriesByPostCount(int limit) {
        return findTopCategoriesByPostCount(PageRequest.of(0, limit));
    }
}
