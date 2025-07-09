package korastudy.be.mapper;

import korastudy.be.dto.response.course.CourseResponse;
import korastudy.be.entity.Course.Course;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = TopicGroupMapper.class)
public interface CourseMapper {

    @Mapping(target = "topicGroups", source = "topicGroups")
    CourseResponse toDTO(Course course);

    List<CourseResponse> toDTOList(List<Course> courses);
}
