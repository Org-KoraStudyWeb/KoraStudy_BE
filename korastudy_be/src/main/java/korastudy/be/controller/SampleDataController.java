package korastudy.be.controller;

import korastudy.be.payload.response.ApiSuccess;
import korastudy.be.service.ISampleDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/sample-data")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class SampleDataController {
    
    private final ISampleDataService sampleDataService;
    
    /**
     * Create sample mock test data
     */
    @PostMapping("/mock-tests")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CONTENT_MANAGER')")
    public ResponseEntity<ApiSuccess> createSampleMockTests() {
        log.info("Creating sample mock test data");
        sampleDataService.createSampleMockTests();
        return ResponseEntity.ok(ApiSuccess.of("Sample mock test data created successfully"));
    }
    
    /**
     * Create sample users and test results
     */
    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiSuccess> createSampleUsers() {
        log.info("Creating sample user data");
        sampleDataService.createSampleUsers();
        return ResponseEntity.ok(ApiSuccess.of("Sample user data created successfully"));
    }
    
    /**
     * Create all sample data
     */
    @PostMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiSuccess> createAllSampleData() {
        log.info("Creating all sample data");
        sampleDataService.createAllSampleData();
        return ResponseEntity.ok(ApiSuccess.of("All sample data created successfully"));
    }
    
    /**
     * Clear all mock test data (for testing)
     */
    @DeleteMapping("/clear")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiSuccess> clearSampleData() {
        log.info("Clearing sample data");
        sampleDataService.clearSampleData();
        return ResponseEntity.ok(ApiSuccess.of("Sample data cleared successfully"));
    }
}
