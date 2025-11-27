package korastudy.be.dto.request.upload;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class DocumentUploadRequest {
    private MultipartFile file;
    private String title;
    private String description;
}