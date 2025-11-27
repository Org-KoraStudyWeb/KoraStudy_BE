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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonService implements ILessonService {

    private final LessonRepository lessonRepository;
    private final SectionRepository sectionRepository;
    private final LessonProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final IUploadService uploadService;

    // ==================== LESSON MANAGEMENT ====================

    @Override
    @Transactional
    public LessonDTO createLesson(LessonCreateRequest request) {
        Section section = sectionRepository.findById(request.getSectionId()).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chương học với ID: " + request.getSectionId()));

        Lesson lesson = Lesson.builder().lessonTitle(request.getLessonTitle()).content(request.getContent()).videoUrl(request.getVideoUrl()).documentUrl(request.getDocumentUrl()).contentType(request.getContentType()).orderIndex(request.getOrderIndex()).duration(request.getDuration()) //  Số giây
                .section(section).build();

        Lesson savedLesson = lessonRepository.save(lesson);
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
        lesson.setDuration(request.getDuration()); //  Số giây
        lesson.setSection(section);

        Lesson updatedLesson = lessonRepository.save(lesson);
        return mapToDTO(updatedLesson);
    }

    // ==================== FILE UPLOAD với TÍNH THỜI GIAN ====================

    @Override
    @Transactional
    public String uploadVideo(MultipartFile file, String title) {
        try {
            // 1. Upload video lên Cloudinary
            String videoUrl = uploadService.uploadVideo(file, title);

            // 2.  THÊM: Tính thời lượng video (giây)
            Integer duration = calculateVideoDuration(file);

            // 3. Log thông tin
            System.out.println("🎥 Video uploaded: " + videoUrl);
            System.out.println("⏱️ Video duration: " + duration + " seconds");
            System.out.println("📊 Formatted: " + formatDuration(duration));

            return videoUrl;

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi upload video: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public String uploadDocument(MultipartFile file, String title) {
        try {
            // Upload document lên Cloudinary
            String documentUrl = uploadService.uploadDocument(file, title);

            System.out.println("📄 Document uploaded: " + documentUrl);

            return documentUrl;

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi upload document: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void deleteFile(String fileUrl) {
        try {
            uploadService.deleteFile(fileUrl);
            System.out.println(" File deleted: " + fileUrl);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xóa file: " + e.getMessage(), e);
        }
    }

    // ==================== TÍNH THỜI LƯỢNG VIDEO ====================

    /**
     *  THÊM: Tính thời lượng video từ file
     * Trong thực tế, cần dùng thư viện như JAVE, FFmpeg, etc.
     * Tạm thời ước tính dựa trên file size và type
     */
    private Integer calculateVideoDuration(MultipartFile file) {
        try {
            long fileSize = file.getSize();
            String contentType = file.getContentType();

            //  ƯỚC TÍNH THỜI LƯỢNG DỰA TRÊN FILE SIZE VÀ TYPE
            if (contentType != null) {
                // Tỷ lệ bitrate ước tính (bits per second)
                double estimatedBitrate = getEstimatedBitrate(contentType, fileSize);

                if (estimatedBitrate > 0) {
                    // Thời lượng = (file size * 8) / bitrate
                    double durationInSeconds = (fileSize * 8.0) / estimatedBitrate;
                    return (int) Math.round(durationInSeconds);
                }
            }

            // Fallback: Ước tính dựa trên file size
            return estimateDurationFromSize(fileSize);

        } catch (Exception e) {
            System.out.println(" Cannot calculate video duration, using default: " + e.getMessage());
            return 300; // Default 5 minutes
        }
    }

    /**
     *  Ước tính bitrate dựa trên loại video
     */
    private double getEstimatedBitrate(String contentType, long fileSize) {
        // Bitrate ước tính cho các loại video (bits per second)
        return switch (contentType) {
            case "video/mp4" ->
                    fileSize > 100 * 1024 * 1024 ? 2000000 : 1000000; // 2 Mbps cho file lớn, 1 Mbps cho file nhỏ
            case "video/avi" -> 1500000; // 1.5 Mbps
            case "video/mov" -> 1800000; // 1.8 Mbps
            case "video/mkv" -> 2200000; // 2.2 Mbps
            default -> 1000000; // 1 Mbps mặc định
        };
    }

    /**
     *  Ước tính thời lượng dựa trên kích thước file (fallback)
     */
    private Integer estimateDurationFromSize(long fileSize) {
        // Ước tính thô: 1MB ≈ 10-15 giây video
        double sizeInMB = fileSize / (1024.0 * 1024.0);

        if (sizeInMB < 5) return 30;      // <5MB: 30 giây
        else if (sizeInMB < 20) return 120; // 5-20MB: 2 phút
        else if (sizeInMB < 50) return 300; // 20-50MB: 5 phút
        else if (sizeInMB < 100) return 600; // 50-100MB: 10 phút
        else return 1200; // >100MB: 20 phút
    }

    /**
     *  Format duration từ giây sang string đẹp
     */
    private String formatDuration(Integer durationInSeconds) {
        if (durationInSeconds == null || durationInSeconds == 0) {
            return "0:00";
        }

        int hours = durationInSeconds / 3600;
        int minutes = (durationInSeconds % 3600) / 60;
        int seconds = durationInSeconds % 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%d:%02d", minutes, seconds);
        }
    }

    // ==================== CÁC METHOD KHÁC GIỮ NGUYÊN ====================

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

        // Xóa file nếu có
        if (lesson.getVideoUrl() != null) {
            deleteFile(lesson.getVideoUrl());
        }
        if (lesson.getDocumentUrl() != null) {
            deleteFile(lesson.getDocumentUrl());
        }

        lessonRepository.delete(lesson);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LessonDTO> getLessonsBySectionId(Long sectionId) {
        List<Lesson> lessons = lessonRepository.findBySectionIdOrderByOrderIndex(sectionId);
        return lessons.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LessonDTO> getLessonsByCourseId(Long courseId) {
        List<Lesson> lessons = lessonRepository.findByCourseId(courseId);
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

        LessonProgress savedProgress = progressRepository.save(progress);
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
        return progresses.stream().map(this::mapToProgressDTO).collect(Collectors.toList());
    }

    @Override
    public LessonDTO mapToDTO(Lesson lesson) {
        return LessonDTO.builder().id(lesson.getId()).lessonTitle(lesson.getLessonTitle()).content(lesson.getContent()).videoUrl(lesson.getVideoUrl()).documentUrl(lesson.getDocumentUrl()).contentType(lesson.getContentType()).orderIndex(lesson.getOrderIndex()).duration(lesson.getDuration()) //  Số giây
                .sectionId(lesson.getSection().getId()).sectionName(lesson.getSection().getSectionName()).build();
    }

    private LessonProgressDTO mapToProgressDTO(LessonProgress progress) {
        return LessonProgressDTO.builder().id(progress.getId()).status(progress.getStatus()).timeSpent(progress.getTimeSpent()).progress(calculateProgress(progress)).startedDate(progress.getStartedDate()).completedDate(progress.getCompletedDate()).lessonId(progress.getLesson().getId()).lessonTitle(progress.getLesson().getLessonTitle()).userId(progress.getUser().getId()).username(progress.getUser().getAccount() != null ? progress.getUser().getAccount().getUsername() : progress.getUser().getDisplayName()).build();
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