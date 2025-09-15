package korastudy.be.service.impl;

import korastudy.be.dto.request.course.SectionCreateRequest;
import korastudy.be.dto.response.course.LessonDTO;
import korastudy.be.dto.response.course.SectionDTO;
import korastudy.be.entity.Course.Course;
import korastudy.be.entity.Course.Section;
import korastudy.be.exception.ResourceNotFoundException;
import korastudy.be.repository.CourseRepository;
import korastudy.be.repository.SectionRepository;
import korastudy.be.service.ILessonService;
import korastudy.be.service.ISectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SectionService implements ISectionService {

    private final SectionRepository sectionRepository;
    private final CourseRepository courseRepository;
    private final ILessonService lessonService;

    @Override
    public SectionDTO createSection(SectionCreateRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học với ID: " + request.getCourseId()));

        Section section = Section.builder()
                .sectionName(request.getSectionName())
                .orderIndex(request.getOrderIndex())
                .course(course)
                .build();
                
        Section savedSection = sectionRepository.save(section);
        return mapToDTO(savedSection);
    }

    @Override
    public SectionDTO updateSection(Long sectionId, SectionCreateRequest request) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chương học với ID: " + sectionId));
                
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học với ID: " + request.getCourseId()));
                
        section.setSectionName(request.getSectionName());
        section.setOrderIndex(request.getOrderIndex());
        section.setCourse(course);
        
        Section updatedSection = sectionRepository.save(section);
        return mapToDTO(updatedSection);
    }

    @Override
    public SectionDTO getSectionById(Long sectionId) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chương học với ID: " + sectionId));
        return mapToDTO(section);
    }

    @Override
    public List<SectionDTO> getSectionsByCourseId(Long courseId) {
        List<Section> sections = sectionRepository.findByCourseIdOrderByOrderIndex(courseId);
        return sections.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteSection(Long sectionId) {
        if (!sectionRepository.existsById(sectionId)) {
            throw new ResourceNotFoundException("Không tìm thấy chương học với ID: " + sectionId);
        }
        sectionRepository.deleteById(sectionId);
    }

    @Override
    public SectionDTO mapToDTO(Section section) {
        List<LessonDTO> lessonDTOs = section.getLessons() != null ?
                section.getLessons().stream()
                        .map(lessonService::mapToDTO)
                        .collect(Collectors.toList()) : 
                List.of();
                
        return SectionDTO.builder()
                .id(section.getId())
                .sectionName(section.getSectionName())
                .orderIndex(section.getOrderIndex())
                .lessons(lessonDTOs)
                .build();
    }
}
