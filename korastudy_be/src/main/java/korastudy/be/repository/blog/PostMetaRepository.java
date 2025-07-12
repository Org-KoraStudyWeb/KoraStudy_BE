package korastudy.be.repository.blog;

import korastudy.be.entity.Post.PostMeta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostMetaRepository extends JpaRepository<PostMeta, Long> {}