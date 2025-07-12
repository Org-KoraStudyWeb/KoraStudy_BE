package korastudy.be.service.impl;

import korastudy.be.dto.request.course.CreateCourseRequest;
import korastudy.be.dto.response.course.CourseResponse;
import korastudy.be.entity.Course.Course;
import korastudy.be.entity.Topic.TopicGroup;
import korastudy.be.mapper.CourseMapper;
import korastudy.be.repository.CourseRepository;
import korastudy.be.repository.TopicGroupRepository;
import korastudy.be.service.ICourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService implements ICourseService {
    private final CourseRepository courseRepository;
    private final TopicGroupRepository topicGroupRepository;
    private final CourseMapper courseMapper;

    @Override
    public List<CourseResponse> getAllCourses() {
        return courseMapper.toDTOList(courseRepository.findAll());
    }

    @Override
    public Page<CourseResponse> getAllCoursesPaged(Pageable pageable) {
        return courseRepository.findAll(pageable)
                .map(courseMapper::toDTO);
    }

    @Override
    public List<CourseResponse> getAllPublishedCourses() {
        return courseMapper.toDTOList(courseRepository.findByIsPublishedTrue());
    }

    @Override
    public Page<CourseResponse> searchCourses(String keyword, Pageable pageable) {
        return courseRepository.searchAllFields(keyword, pageable)
                .map(courseMapper::toDTO);
    }

    @Override
    public CourseResponse getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học"));
        return courseMapper.toDTO(course);
    }

    @Override
    public Course createCourse(CreateCourseRequest dto) {
        Course course = Course.builder()
                .name(dto.getName())
                .level(dto.getLevel())
                .description(dto.getDescription())
                .imageUrl(dto.getImageUrl())
                .price(dto.getPrice())
                .isPublished(dto.getIsPublished())
                .build();

        if (dto.getTopicGroups() != null) {
            List<TopicGroup> groups = dto.getTopicGroups().stream()
                    .map(groupDTO -> TopicGroup.builder()
                            .groupName(groupDTO.getGroupName())
                            .description(groupDTO.getDescription())
                            .course(course)
                            .build())
                    .collect(Collectors.toList());
            course.setTopicGroups(groups);
        }

        return courseRepository.save(course);
    }

    @Override
    public Course updateCourse(Long id, CreateCourseRequest dto) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học"));

        course.setName(dto.getName());
        course.setLevel(dto.getLevel());
        course.setDescription(dto.getDescription());
        course.setImageUrl(dto.getImageUrl());
        course.setPrice(dto.getPrice());
        course.setIsPublished(dto.getIsPublished());

        return courseRepository.save(course);
    }

    @Override
    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy khóa học");
        }
        courseRepository.deleteById(id);
    }
}
