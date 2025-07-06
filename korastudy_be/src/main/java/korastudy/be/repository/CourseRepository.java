package korastudy.be.repository;

import korastudy.be.entity.Course.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {


    //Lấy danh sách khóa học đã publish
    List<Course> findByIsPublishedTrue();

    //Lấy danh sách khóa học đã publish
    boolean existsById(Long id);

    //Lấy chi tiết khóa học nếu đã publish
    Optional<Course> findByIdAndIsPublishedTrue(Long id);

    //	Tránh trùng tên khóa học khi tạo mới
    boolean existsByName(String name);


    //Tìm kiếm theo tên khóa học (không phân biệt hoa/thường)
    @Query("SELECT c FROM Course c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Course> searchByKeyword(@Param("keyword") String keyword);

    //Tìm kiếm theo nhiều trường (name, level, description – không phân biệt hoa thường)
    @Query("""
                SELECT c FROM Course c 
                WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(c.level) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    List<Course> searchAllFields(@Param("keyword") String keyword);

    //Tìm kiếm + phân trang kết hợp
    @Query("""
                SELECT c FROM Course c 
                WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(c.level) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    Page<Course> searchAllFields(@Param("keyword") String keyword, Pageable pageable);
}
