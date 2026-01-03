package korastudy.be.service.impl;

import korastudy.be.entity.news.NewsArticle;
import korastudy.be.entity.news.NewsTopic;
import korastudy.be.repository.news.NewsArticleRepository;
import korastudy.be.repository.news.NewsTopicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsWebCrawlerService {

    private final NewsArticleRepository articleRepository;
    private final NewsTopicRepository topicRepository;

//    /**
//     * AUTO CRAWL: Chạy mỗi 6 giờ
//     * Cron: 0 0 */6 * * * = 00:00, 06:00, 12:00, 18:00
//     * Tắt comment nếu không muốn auto crawl
//     */
     @Scheduled(cron = "0 0 */6 * * *")
    @Transactional
    public void scheduledCrawl() {
        log.info("🕷️ Starting scheduled news crawl...");
        try {
            List<NewsArticle> voaArticles = crawlVOAKorea(20);
            
            articleRepository.saveAll(voaArticles);
            
            log.info("✅ Crawled {} articles successfully", voaArticles.size());
        } catch (Exception e) {
            log.error("❌ Crawl failed: {}", e.getMessage(), e);
        }
    }

    /**
     * CRAWL VOA KOREA
     * Source: https://www.voakorea.com
     */
    public List<NewsArticle> crawlVOAKorea(int limit) throws IOException {
        log.info("📰 Crawling VOA Korea...");
        List<NewsArticle> articles = new ArrayList<>();

        String baseUrl = "https://www.voakorea.com";
        String newsUrl = baseUrl + "/z/3910"; // Korean news section

        try {
            Document doc = Jsoup.connect(newsUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .timeout(15000)
                .get();

            // Find or create news topic
            NewsTopic topic = topicRepository.findByTitle("Tin tức thế giới")
                .orElseGet(() -> createTopic("Tin tức thế giới", "🌍"));

            Elements articleElements = doc.select(".media-block");
            int count = 0;

            for (Element elem : articleElements) {
                if (count >= limit) break;

                try {
                    // Extract basic info
                    String title = elem.select(".title a").text();
                    if (title.isEmpty()) continue;

                    String articleUrl = baseUrl + elem.select(".title a").attr("href");
                    String thumbnailUrl = elem.select("img").attr("data-src");
                    if (thumbnailUrl.isEmpty()) {
                        thumbnailUrl = elem.select("img").attr("src");
                    }

                    // Skip if already exists
                    if (articleRepository.existsBySourceUrl(articleUrl)) {
                        log.debug("⏭️ Skipping duplicate: {}", title);
                        continue;
                    }

                    // Fetch full article content
                    Document articleDoc = Jsoup.connect(articleUrl)
                        .userAgent("Mozilla/5.0")
                        .timeout(15000)
                        .get();

                    // Extract content
                    Elements contentParagraphs = articleDoc.select(".wsw p");
                    StringBuilder content = new StringBuilder();
                    for (Element p : contentParagraphs) {
                        String text = p.text();
                        if (!text.isEmpty()) {
                            content.append(text).append("\n\n");
                        }
                    }

                    String fullContent = content.toString().trim();
                    if (fullContent.isEmpty() || fullContent.length() < 100) {
                        log.warn("⚠️ Content too short for: {}", title);
                        continue;
                    }

                    // Extract published date
                    String dateStr = articleDoc.select("time").attr("datetime");
                    LocalDateTime publishedAt = parseDate(dateStr);

                    // Analyze difficulty
                    String difficulty = analyzeDifficulty(fullContent);

                    // Create article entity (without translation for now)
                    NewsArticle article = NewsArticle.builder()
                        .newsTopic(topic)
                        .title(title)
                        .titleVietnamese("") // Will translate later
                        .content(fullContent)
                        .contentVietnamese("") // Will translate later
                        .summary(generateSummary(fullContent))
                        .source("VOA Korea")
                        .sourceUrl(articleUrl)
                        .difficultyLevel(difficulty)
                        .thumbnailUrl(thumbnailUrl)
                        .publishedAt(publishedAt)
                        .build();

                    articles.add(article);
                    count++;

                    log.info("✅ Crawled #{}: {}", count, title);

                    // Be nice to server
                    Thread.sleep(2000);

                } catch (Exception e) {
                    log.error("❌ Failed to crawl article: {}", e.getMessage());
                }
            }

        } catch (IOException e) {
            log.error("❌ Failed to connect to VOA Korea: {}", e.getMessage());
            throw e;
        }

        return articles;
    }

    /**
     * CRAWL KBS NEWS
     * Source: https://news.kbs.co.kr
     */
    public List<NewsArticle> crawlKBSNews(int limit) throws IOException {
        log.info("📰 Crawling KBS News...");
        List<NewsArticle> articles = new ArrayList<>();

        String newsUrl = "https://news.kbs.co.kr/news/list.do?icd=19";

        try {
            Document doc = Jsoup.connect(newsUrl)
                .userAgent("Mozilla/5.0")
                .timeout(15000)
                .get();

            NewsTopic topic = topicRepository.findByTitle("Tin tức Hàn Quốc")
                .orElseGet(() -> createTopic("Tin tức Hàn Quốc", "🇰🇷"));

            Elements newsItems = doc.select(".list-news li");
            int count = 0;

            for (Element item : newsItems) {
                if (count >= limit) break;

                try {
                    String title = item.select(".tit-txt").text();
                    if (title.isEmpty()) continue;

                    String articleUrl = "https://news.kbs.co.kr" + item.select("a").attr("href");
                    String thumbnailUrl = item.select("img").attr("src");

                    if (articleRepository.existsBySourceUrl(articleUrl)) {
                        continue;
                    }

                    // Fetch full content
                    Document articleDoc = Jsoup.connect(articleUrl)
                        .userAgent("Mozilla/5.0")
                        .timeout(15000)
                        .get();

                    String content = articleDoc.select(".detail-body").text();
                    if (content.isEmpty() || content.length() < 100) {
                        continue;
                    }

                    NewsArticle article = NewsArticle.builder()
                        .newsTopic(topic)
                        .title(title)
                        .titleVietnamese("")
                        .content(content)
                        .contentVietnamese("")
                        .summary(generateSummary(content))
                        .source("KBS News")
                        .sourceUrl(articleUrl)
                        .difficultyLevel(analyzeDifficulty(content))
                        .thumbnailUrl(thumbnailUrl)
                        .publishedAt(LocalDateTime.now())
                        .build();

                    articles.add(article);
                    count++;

                    log.info("✅ Crawled #{}: {}", count, title);
                    Thread.sleep(2000);

                } catch (Exception e) {
                    log.error("❌ Failed: {}", e.getMessage());
                }
            }

        } catch (IOException e) {
            log.error("❌ Failed to connect to KBS News: {}", e.getMessage());
            throw e;
        }

        return articles;
    }

    /**
     * Phân tích độ khó dựa trên độ dài và từ vựng
     */
    private String analyzeDifficulty(String content) {
        int length = content.length();

        // Simple heuristic based on length
        if (length < 500) return "BEGINNER";
        if (length < 1500) return "INTERMEDIATE";
        return "ADVANCED";

        // TODO: Nâng cao - dùng Azure AI để phân tích TOPIK level
    }

    /**
     * Tạo summary ngắn (300 ký tự đầu)
     */
    private String generateSummary(String content) {
        if (content.length() <= 300) {
            return content;
        }
        return content.substring(0, 297) + "...";
    }

    /**
     * Parse date string to LocalDateTime
     */
    private LocalDateTime parseDate(String dateStr) {
        try {
            if (dateStr != null && !dateStr.isEmpty()) {
                return LocalDateTime.parse(dateStr);
            }
        } catch (Exception e) {
            log.debug("Failed to parse date: {}", dateStr);
        }
        return LocalDateTime.now();
    }

    /**
     * Create new topic
     */
    private NewsTopic createTopic(String title, String icon) {
        NewsTopic topic = new NewsTopic();
        topic.setTitle(title);
        topic.setIcon(icon);
        topic.setDescription(title);
        return topicRepository.save(topic);
    }

    /**
     * MANUAL CRAWL API - Gọi thủ công khi cần
     */
    @Transactional
    public int crawlNow(String source, int limit) {
        try {
            List<NewsArticle> articles = switch (source.toLowerCase()) {
                case "voa" -> crawlVOAKorea(limit);
                case "kbs" -> crawlKBSNews(limit);
                default -> new ArrayList<>();
            };

            articleRepository.saveAll(articles);
            return articles.size();

        } catch (Exception e) {
            log.error("Manual crawl failed: {}", e.getMessage());
            return 0;
        }
    }
}
