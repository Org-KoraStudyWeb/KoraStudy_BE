package korastudy.be.mapper;

import korastudy.be.dto.response.topic.TopicGroupResponseDTO;
import korastudy.be.entity.Topic.TopicGroup;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TopicGroupMapper {

    TopicGroupResponseDTO toDTO(TopicGroup group);

    List<TopicGroupResponseDTO> toDTOList(List<TopicGroup> groups);
}

