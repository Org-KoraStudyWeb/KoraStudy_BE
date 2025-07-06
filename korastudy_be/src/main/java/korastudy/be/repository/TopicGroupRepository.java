package korastudy.be.repository;

import korastudy.be.entity.Topic.TopicGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TopicGroupRepository extends JpaRepository<TopicGroup, Long> {
    List<TopicGroup> findByCourseId(Long courseId);
}
