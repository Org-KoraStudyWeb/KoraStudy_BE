package korastudy.be.controller;

import korastudy.be.service.impl.NewsWebCrawlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/crawler")
@RequiredArgsConstructor
public class NewsCrawlerController {

    private final NewsWebCrawlerService crawlerService;

    @PostMapping("/crawl")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> manualCrawl(
            @RequestParam String source,
            @RequestParam(defaultValue = "10") int limit
    ) {
        int count = crawlerService.crawlNow(source, limit);

        return ResponseEntity.ok(Map.of(
            "message", "Crawl completed",
            "source", source,
            "articlesAdded", count
        ));
    }
}
