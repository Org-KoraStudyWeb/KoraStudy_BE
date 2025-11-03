package korastudy.be.controller;

import jakarta.validation.Valid;
import korastudy.be.dto.request.course.SectionCreateRequest;
import korastudy.be.dto.response.course.SectionDTO;
import korastudy.be.payload.response.ApiSuccess;
import korastudy.be.service.ISectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sections")
@RequiredArgsConstructor
public class SectionController {

    private final ISectionService sectionService;

    @GetMapping("/{id}")
    public ResponseEntity<SectionDTO> getSectionById(@PathVariable Long id) {
        SectionDTO section = sectionService.getSectionById(id);
        return ResponseEntity.ok(section);
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<SectionDTO>> getSectionsByCourseId(@PathVariable Long courseId) {
        List<SectionDTO> sections = sectionService.getSectionsByCourseId(courseId);
        return ResponseEntity.ok(sections);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<SectionDTO> createSection(@Valid @RequestBody SectionCreateRequest request) {
        SectionDTO sectionDTO = sectionService.createSection(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(sectionDTO);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<SectionDTO> updateSection(@PathVariable Long id, @Valid @RequestBody SectionCreateRequest request) {
        SectionDTO sectionDTO = sectionService.updateSection(id, request);
        return ResponseEntity.ok(sectionDTO);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CONTENT_MANAGER', 'ADMIN')")
    public ResponseEntity<ApiSuccess> deleteSection(@PathVariable Long id) {
        sectionService.deleteSection(id);
        return ResponseEntity.ok(ApiSuccess.of("Xóa chương học thành công"));
    }
}
