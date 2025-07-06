package korastudy.be.service.impl;

import korastudy.be.dto.response.topic.TopicGroupResponseDTO;
import korastudy.be.repository.TopicGroupRepository;
import korastudy.be.service.ITopicGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TopicGroupService implements ITopicGroupService {
    private final TopicGroupRepository topicGroupRepository;

    @Override
    public List<TopicGroupResponseDTO> getTopicGroupsByCourseId(Long courseId) {
        return topicGroupRepository.findByCourseId(courseId).stream()
                .map(group -> TopicGroupResponseDTO.builder()
                        .id(group.getId())
                        .groupName(group.getGroupName())
                        .description(group.getDescription())
                        .build())
                .collect(Collectors.toList());
    }

}
