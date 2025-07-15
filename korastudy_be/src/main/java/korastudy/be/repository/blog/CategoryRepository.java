package korastudy.be.repository.blog;

import korastudy.be.entity.Post.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
