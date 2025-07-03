package korastudy.be.repository;

import korastudy.be.entity.Course.MyCourse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MyCourseRepository extends JpaRepository<MyCourse, Long> {
}
