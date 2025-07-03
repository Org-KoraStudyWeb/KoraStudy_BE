package korastudy.be.service;

import korastudy.be.entity.Course.Course;

import java.util.List;

public interface ICourseService {

    //Hiển thị khóa học với các admin và deliveryManager
    List<Course> getAllPublishedCourses();

    //Tìm kiếm khóa học
    List<Course> searchCourses(String keyword);

    Course getCourseById(Long id);

    //Thêm khóa học với quyền admin và deliveryManager
    Course createCourse(Course course);

    //Update khóa học với quyền admin và deliveryManager
    Course updateCourse(Course course);

    //Delete khóa học với quyền admin và deliveryManager
    void deleteCourse(Course course);
}
