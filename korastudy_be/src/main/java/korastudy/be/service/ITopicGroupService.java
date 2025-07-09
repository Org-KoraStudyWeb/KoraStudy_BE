package korastudy.be.service;

import korastudy.be.dto.response.topic.TopicGroupResponseDTO;

import java.util.List;

public interface ITopicGroupService {
    List<TopicGroupResponseDTO> getTopicGroupsByCourseId(Long courseId);

}