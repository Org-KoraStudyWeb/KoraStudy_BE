package korastudy.be.service.impl;

import korastudy.be.dto.request.course.*;
import korastudy.be.dto.response.course.*;
import korastudy.be.entity.Course.*;
import korastudy.be.entity.Enum.ProgressStatus;
import korastudy.be.entity.User.User;
import korastudy.be.exception.ResourceNotFoundException;
import korastudy.be.repository.*;
import korastudy.be.service.ILessonService;
import korastudy.be.service.IUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LessonService implements ILessonService {

    private final LessonRepository lessonRepository;
    private final SectionRepository sectionRepository;
    private final LessonProgressRepository progressRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final IUploadService uploadService;

    // ==================== LESSON MANAGEMENT ====================

    @Override
    @Transactional
    public LessonDTO createLesson(LessonCreateRequest request) {
        Section section = sectionRepository.findById(request.getSectionId()).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chương học với ID: " + request.getSectionId()));

        Lesson lesson = Lesson.builder().lessonTitle(request.getLessonTitle()).content(request.getContent()).videoUrl(request.getVideoUrl()).documentUrl(request.getDocumentUrl()).contentType(request.getContentType()).orderIndex(request.getOrderIndex()).duration(request.getDuration()).section(section).build();

        Lesson savedLesson = lessonRepository.save(lesson);
        log.info("Created lesson: ID={}, Title={}", savedLesson.getId(), savedLesson.getLessonTitle());
        return mapToDTO(savedLesson);
    }

    @Override
    @Transactional
    public LessonDTO updateLesson(Long id, LessonUpdateRequest request) {
        Lesson lesson = lessonRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài học với ID: " + id));

        Section section = sectionRepository.findById(request.getSectionId()).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chương học với ID: " + request.getSectionId()));

        lesson.setLessonTitle(request.getLessonTitle());
        lesson.setContent(request.getContent());
        lesson.setVideoUrl(request.getVideoUrl());
        lesson.setDocumentUrl(request.getDocumentUrl());
        lesson.setContentType(request.getContentType());
        lesson.setOrderIndex(request.getOrderIndex());
        lesson.setDuration(request.getDuration());
        lesson.setSection(section);

        Lesson updatedLesson = lessonRepository.save(lesson);
        log.info("Updated lesson: ID={}, Title={}", updatedLesson.getId(), updatedLesson.getLessonTitle());
        return mapToDTO(updatedLesson);
    }

    // ==================== FILE UPLOAD ====================

    @Override
    @Transactional
    public String uploadVideo(MultipartFile file, String title) {
        try {
            String videoUrl = uploadService.uploadVideo(file, title);
            Integer duration = calculateVideoDuration(file);

            log.info("Video uploaded: URL={}, Duration={} seconds, Title={}", videoUrl, duration, title);
            return videoUrl;

        } catch (Exception e) {
            log.error("Error uploading video: {}", e.getMessage());
            throw new RuntimeException("Lỗi khi upload video: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public String uploadDocument(MultipartFile file, String title) {
        try {
            String documentUrl = uploadService.uploadDocument(file, title);
            log.info("Document uploaded: URL={}, Title={}", documentUrl, title);
            return documentUrl;

        } catch (Exception e) {
            log.error("Error uploading document: {}", e.getMessage());
            throw new RuntimeException("Lỗi khi upload document: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void deleteFile(String fileUrl) {
        try {
            uploadService.deleteFile(fileUrl);
            log.info("File deleted: URL={}", fileUrl);
        } catch (Exception e) {
            log.error("Error deleting file: {}", e.getMessage());
            throw new RuntimeException("Lỗi khi xóa file: " + e.getMessage(), e);
        }
    }

    // ==================== VIDEO DURATION CALCULATION ====================

    private Integer calculateVideoDuration(MultipartFile file) {
        try {
            long fileSize = file.getSize();
            String contentType = file.getContentType();

            if (contentType != null) {
                double estimatedBitrate = getEstimatedBitrate(contentType, fileSize);
                if (estimatedBitrate > 0) {
                    double durationInSeconds = (fileSize * 8.0) / estimatedBitrate;
                    return (int) Math.round(durationInSeconds);
                }
            }

            return estimateDurationFromSize(fileSize);

        } catch (Exception e) {
            log.warn("Cannot calculate video duration, using default: {}", e.getMessage());
            return 300; // Default 5 minutes
        }
    }

    private double getEstimatedBitrate(String contentType, long fileSize) {
        return switch (contentType) {
            case "video/mp4" -> fileSize > 100 * 1024 * 1024 ? 2000000 : 1000000;
            case "video/avi" -> 1500000;
            case "video/mov" -> 1800000;
            case "video/mkv" -> 2200000;
            default -> 1000000;
        };
    }

    private Integer estimateDurationFromSize(long fileSize) {
        double sizeInMB = fileSize / (1024.0 * 1024.0);

        if (sizeInMB < 5) return 30;
        else if (sizeInMB < 20) return 120;
        else if (sizeInMB < 50) return 300;
        else if (sizeInMB < 100) return 600;
        else return 1200;
    }

    // ==================== BASIC CRUD OPERATIONS ====================

    @Override
    @Transactional(readOnly = true)
    public LessonDTO getLessonById(Long id) {
        Lesson lesson = lessonRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài học với ID: " + id));
        return mapToDTO(lesson);
    }

    @Override
    @Transactional
    public void deleteLesson(Long id) {
        Lesson lesson = lessonRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài học với ID: " + id));

        if (lesson.getVideoUrl() != null) {
            deleteFile(lesson.getVideoUrl());
        }
        if (lesson.getDocumentUrl() != null) {
            deleteFile(lesson.getDocumentUrl());
        }

        lessonRepository.delete(lesson);
        log.info("Deleted lesson: ID={}, Title={}", lesson.getId(), lesson.getLessonTitle());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LessonDTO> getLessonsBySectionId(Long sectionId) {
        List<Lesson> lessons = lessonRepository.findBySectionIdOrderByOrderIndex(sectionId);
        log.info("Found {} lessons for section ID={}", lessons.size(), sectionId);
        return lessons.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LessonDTO> getLessonsByCourseId(Long courseId) {
        List<Lesson> lessons = lessonRepository.findByCourseId(courseId);
        log.info("Found {} lessons for course ID={}", lessons.size(), courseId);
        return lessons.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    // ==================== PROGRESS TRACKING ====================

    @Override
    @Transactional
    public LessonProgressDTO updateLessonProgress(LessonProgressRequest request, String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user với username: " + username));

        Lesson lesson = lessonRepository.findById(request.getLessonId()).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài học với ID: " + request.getLessonId()));

        LessonProgress progress = progressRepository.findByUserIdAndLessonId(user.getId(), request.getLessonId()).orElse(LessonProgress.builder().user(user).lesson(lesson).status(ProgressStatus.NOT_STARTED).timeSpent(0L).build());

        if (request.getStatus() != null) {
            progress.setStatus(ProgressStatus.valueOf(request.getStatus()));
        }

        if (request.getTimeSpent() != null) {
            progress.setTimeSpent(request.getTimeSpent());
        }

        if (progress.getStartedDate() == null && (progress.getStatus() == ProgressStatus.IN_PROGRESS || progress.getStatus() == ProgressStatus.COMPLETED)) {
            progress.setStartedDate(LocalDateTime.now());
        }

        if (progress.getStatus() == ProgressStatus.COMPLETED && progress.getCompletedDate() == null) {
            progress.setCompletedDate(LocalDateTime.now());
        }

        progress.setLastAccessed(LocalDateTime.now());

        LessonProgress savedProgress = progressRepository.save(progress);
        log.info("Updated progress: User={}, Lesson={}, Status={}", username, request.getLessonId(), progress.getStatus());
        return mapToProgressDTO(savedProgress);
    }

    @Override
    @Transactional(readOnly = true)
    public LessonProgressDTO getLessonProgress(Long lessonId, String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user với username: " + username));

        Lesson lesson = lessonRepository.findById(lessonId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài học với ID: " + lessonId));

        LessonProgress progress = progressRepository.findByUserIdAndLessonId(user.getId(), lessonId).orElse(LessonProgress.builder().user(user).lesson(lesson).status(ProgressStatus.NOT_STARTED).timeSpent(0L).build());

        return mapToProgressDTO(progress);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LessonProgressDTO> getUserProgressByCourse(Long courseId, String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user với username: " + username));

        List<LessonProgress> progresses = progressRepository.findByUserIdAndLessonSectionCourseId(user.getId(), courseId);
        log.info("Found {} progress records for user {} in course {}", progresses.size(), username, courseId);
        return progresses.stream().map(this::mapToProgressDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LessonProgressDTO> getUserProgressByCourseForAdmin(Long courseId, Long userId) {
        try {
            log.info("[ADMIN] Getting progress for user {} in course {}", userId, courseId);

            List<LessonProgress> userProgresses = progressRepository.findByCourseIdAndUserId(courseId, userId);

            if (userProgresses.isEmpty()) {
                log.info("[ADMIN] User {} has no progress in course {}", userId, courseId);
                return List.of();
            }

            List<LessonProgressDTO> progressDTOs = userProgresses.stream().map(this::mapToProgressDTO).collect(Collectors.toList());

            log.info("[ADMIN] Found {} progress records for user {} in course {}", progressDTOs.size(), userId, courseId);
            return progressDTOs;

        } catch (Exception e) {
            log.error("[ADMIN] Error getting progress for user {} in course {}: {}", userId, courseId, e.getMessage());
            throw new RuntimeException("Lỗi khi lấy tiến độ học tập: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<LessonProgressDTO> getAllUsersProgressByCourse(Long courseId) {
        try {
            log.info("[ADMIN] Getting all users progress for course {}", courseId);

            List<LessonProgress> allProgress = progressRepository.findByCourseId(courseId);

            if (allProgress.isEmpty()) {
                log.info("[ADMIN] Course {} has no progress records", courseId);
                return List.of();
            }

            List<LessonProgressDTO> progressDTOs = allProgress.stream().map(this::mapToProgressDTO).collect(Collectors.toList());

            log.info("[ADMIN] Found {} progress records for course {}", progressDTOs.size(), courseId);
            return progressDTOs;

        } catch (Exception e) {
            log.error("[ADMIN] Error getting all users progress for course {}: {}", courseId, e.getMessage());
            throw new RuntimeException("Lỗi khi lấy tiến độ tất cả học viên: " + e.getMessage());
        }
    }

    // ==================== MAPPING METHODS ====================

    @Override
    public LessonDTO mapToDTO(Lesson lesson) {
        return LessonDTO.builder().id(lesson.getId()).lessonTitle(lesson.getLessonTitle()).content(lesson.getContent()).videoUrl(lesson.getVideoUrl()).documentUrl(lesson.getDocumentUrl()).contentType(lesson.getContentType()).orderIndex(lesson.getOrderIndex()).duration(lesson.getDuration()).sectionId(lesson.getSection().getId()).sectionName(lesson.getSection().getSectionName()).build();
    }

    private LessonProgressDTO mapToProgressDTO(LessonProgress progress) {
        return LessonProgressDTO.builder().id(progress.getId()).status(progress.getStatus()).timeSpent(progress.getTimeSpent()).progress(calculateProgress(progress)).startedDate(progress.getStartedDate()).completedDate(progress.getCompletedDate()).lastAccessed(progress.getLastAccessed()).lessonId(progress.getLesson().getId()).lessonTitle(progress.getLesson().getLessonTitle()).userId(progress.getUser().getId()).username(progress.getUser().getAccount() != null ? progress.getUser().getAccount().getUsername() : progress.getUser().getDisplayName()).build();
    }

    private Double calculateProgress(LessonProgress progress) {
        if (progress.getStatus() == ProgressStatus.COMPLETED) {
            return 100.0;
        } else if (progress.getStatus() == ProgressStatus.IN_PROGRESS) {
            Long lessonDuration = progress.getLesson().getDuration() != null ? progress.getLesson().getDuration().longValue() : 0L;

            if (lessonDuration > 0) {
                double percentage = ((double) progress.getTimeSpent() / lessonDuration) * 100;
                return Math.min(99.9, Math.max(0, percentage));
            }
            return 50.0;
        }
        return 0.0;
    }
}