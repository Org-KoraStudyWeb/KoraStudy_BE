package korastudy.be.repository.blog;

import korastudy.be.entity.Post.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {}