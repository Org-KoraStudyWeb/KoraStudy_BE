package korastudy.be.service;

import korastudy.be.dto.request.course.SectionCreateRequest;
import korastudy.be.dto.response.course.SectionDTO;
import korastudy.be.entity.Course.Section;

import java.util.List;

public interface ISectionService {
    
    SectionDTO createSection(SectionCreateRequest request);
    
    SectionDTO updateSection(Long sectionId, SectionCreateRequest request);
    
    SectionDTO getSectionById(Long sectionId);
    
    List<SectionDTO> getSectionsByCourseId(Long courseId);
    
    void deleteSection(Long sectionId);
    
    SectionDTO mapToDTO(Section section);
}
