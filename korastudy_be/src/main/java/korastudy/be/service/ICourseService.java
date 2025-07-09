package korastudy.be.service;

import korastudy.be.dto.request.course.CreateCourseRequest;
import korastudy.be.dto.response.course.CourseResponse;
import korastudy.be.entity.Course.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ICourseService {

    // Admin và deliveryManager xem tất cả khóa học
    List<CourseResponse> getAllCourses();

    //hiển thị list có phân trang
    Page<CourseResponse> getAllCoursesPaged(Pageable pageable);

    // Chỉ lấy những khóa học đã publish (cho học viên / khách truy cập)
    List<CourseResponse> getAllPublishedCourses();

    // Tìm kiếm có phân trang
    Page<CourseResponse> searchCourses(String keyword, Pageable pageable);

    // Lấy chi tiết 1 khóa học
    CourseResponse getCourseById(Long id);

    // Tạo khóa học
    Course createCourse(CreateCourseRequest dto);

    // Cập nhật khóa học
    Course updateCourse(Long id, CreateCourseRequest dto);

    // Xóa khóa học
    void deleteCourse(Long id);
}
