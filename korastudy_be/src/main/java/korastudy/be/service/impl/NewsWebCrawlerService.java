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
            log.info("🌐 Connecting to: {}", newsUrl);
            Document doc = Jsoup.connect(newsUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(15000)
                .get();
            
            log.info("✅ HTML fetched, length: {} chars", doc.html().length());

            NewsTopic topic = topicRepository.findByTitle("Tin tức Hàn Quốc")
                .orElseGet(() -> createTopic("Tin tức Hàn Quốc", "🇰🇷"));

            Elements newsItems = doc.select(".list-news li");
            log.info("📋 Found {} news items with selector '.list-news li'", newsItems.size());
            
            if (newsItems.isEmpty()) {
                // Try alternative selectors
                newsItems = doc.select("ul.list li");
                log.info("📋 Trying alternative selector 'ul.list li': {} items", newsItems.size());
            }
            
            if (newsItems.isEmpty()) {
                newsItems = doc.select("div.news-list article");
                log.info("📋 Trying alternative selector 'div.news-list article': {} items", newsItems.size());
            }
            
            int count = 0;

            for (Element item : newsItems) {
                if (count >= limit) break;

                try {
                    String title = item.select(".tit-txt").text();
                    if (title.isEmpty()) {
                        title = item.select("a").text();
                    }
                    if (title.isEmpty()) {
                        log.debug("⏭️ Skipping item with no title");
                        continue;
                    }

                    String articleUrl = "https://news.kbs.co.kr" + item.select("a").attr("href");
                    String thumbnailUrl = item.select("img").attr("src");
                    
                    log.debug("🔗 Article URL: {}", articleUrl);

                    if (articleRepository.existsBySourceUrl(articleUrl)) {
                        log.debug("⏭️ Skipping duplicate: {}", title);
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
     * CRAWL SINGLE ARTICLE - Admin nhập URL để crawl 1 bài cụ thể
     * Cải thiện: Giữ format HTML tốt hơn, crawl nhiều ảnh
     */
    public NewsArticle crawlSingleArticle(String url) {
        try {
            log.info("🔗 Crawling single article from: {}", url);
            
            Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(15000)
                .get();
            
            String source;
            String title;
            String content;
            String contentHtml; // Giữ HTML format
            String thumbnailUrl = "";
            List<String> imageUrls = new ArrayList<>();
            
            if (url.contains("voakorea.com")) {
                source = "VOA Korea";
                title = doc.select("h1.title, .page-title, h1").first() != null ? 
                    doc.select("h1.title, .page-title, h1").first().text() : "No title";
                
                // Lấy content element
                Element contentElement = doc.select(".wsw").first();
                if (contentElement != null) {
                    // Xử lý từng paragraph để giữ format
                    StringBuilder plainText = new StringBuilder();
                    StringBuilder htmlText = new StringBuilder();
                    
                    for (Element elem : contentElement.children()) {
                        if (elem.tagName().equals("p")) {
                            String text = elem.text().trim();
                            if (!text.isEmpty()) {
                                plainText.append(text).append("\n\n");
                                htmlText.append("<p>").append(text).append("</p>\n");
                            }
                        } else if (elem.tagName().equals("figure") || elem.tagName().equals("div")) {
                            // Lấy ảnh trong bài
                            Elements imgs = elem.select("img");
                            for (Element img : imgs) {
                                String imgSrc = img.attr("data-src");
                                if (imgSrc.isEmpty()) imgSrc = img.attr("src");
                                if (!imgSrc.isEmpty() && !imgSrc.contains("data:image")) {
                                    if (!imgSrc.startsWith("http")) {
                                        imgSrc = "https://www.voakorea.com" + imgSrc;
                                    }
                                    imageUrls.add(imgSrc);
                                    htmlText.append("<figure><img src=\"").append(imgSrc).append("\" /></figure>\n");
                                }
                            }
                        }
                    }
                    content = plainText.toString().trim();
                    contentHtml = htmlText.toString();
                } else {
                    content = "";
                    contentHtml = "";
                }
                
                // Thumbnail - ảnh đầu tiên
                Element mainImg = doc.select(".media-pholder img, .cover-media img, article img").first();
                if (mainImg != null) {
                    thumbnailUrl = mainImg.attr("data-src");
                    if (thumbnailUrl.isEmpty()) thumbnailUrl = mainImg.attr("src");
                    if (!thumbnailUrl.isEmpty() && !thumbnailUrl.startsWith("http")) {
                        thumbnailUrl = "https://www.voakorea.com" + thumbnailUrl;
                    }
                }
                
            } else if (url.contains("kbs.co.kr")) {
                source = "KBS News";
                title = doc.select(".headline-title, h1.tit, .detail-headline h2, h1").first() != null ?
                    doc.select(".headline-title, h1.tit, .detail-headline h2, h1").first().text() : "No title";
                
                Element contentElement = doc.select(".detail-body, .article-body, #cont_newstext").first();
                if (contentElement != null) {
                    StringBuilder plainText = new StringBuilder();
                    StringBuilder htmlText = new StringBuilder();
                    
                    for (Element elem : contentElement.children()) {
                        if (elem.tagName().equals("p") || elem.tagName().equals("div")) {
                            String text = elem.text().trim();
                            if (!text.isEmpty() && text.length() > 10) {
                                plainText.append(text).append("\n\n");
                                htmlText.append("<p>").append(text).append("</p>\n");
                            }
                        }
                    }
                    
                    // Nếu không có paragraphs, lấy toàn bộ text
                    if (plainText.length() == 0) {
                        content = contentElement.text();
                        contentHtml = "<p>" + content + "</p>";
                    } else {
                        content = plainText.toString().trim();
                        contentHtml = htmlText.toString();
                    }
                    
                    // Lấy ảnh
                    for (Element img : contentElement.select("img")) {
                        String imgSrc = img.attr("src");
                        if (!imgSrc.isEmpty() && !imgSrc.contains("data:image")) {
                            if (!imgSrc.startsWith("http")) {
                                imgSrc = "https://news.kbs.co.kr" + imgSrc;
                            }
                            imageUrls.add(imgSrc);
                        }
                    }
                } else {
                    content = "";
                    contentHtml = "";
                }
                
                Element mainImg = doc.select(".detail-image img, .article-image img, .photo-box img").first();
                if (mainImg != null) {
                    thumbnailUrl = mainImg.attr("src");
                    if (!thumbnailUrl.isEmpty() && !thumbnailUrl.startsWith("http")) {
                        thumbnailUrl = "https://news.kbs.co.kr" + thumbnailUrl;
                    }
                }
                
            } else {
                // Generic crawler - cải thiện cho nhiều trang khác
                source = extractDomain(url);
                
                // Tìm title
                Element titleElem = doc.select("h1, .article-title, .post-title, .entry-title").first();
                title = titleElem != null ? titleElem.text() : doc.title();
                
                // Tìm content container
                Element contentElement = doc.select("article, .article-content, .post-content, .entry-content, .content, main").first();
                
                if (contentElement != null) {
                    StringBuilder plainText = new StringBuilder();
                    StringBuilder htmlText = new StringBuilder();
                    
                    for (Element p : contentElement.select("p")) {
                        String text = p.text().trim();
                        if (!text.isEmpty() && text.length() > 20) {
                            plainText.append(text).append("\n\n");
                            htmlText.append("<p>").append(text).append("</p>\n");
                        }
                    }
                    
                    content = plainText.toString().trim();
                    contentHtml = htmlText.toString();
                    
                    // Lấy ảnh
                    for (Element img : contentElement.select("img")) {
                        String imgSrc = img.attr("src");
                        if (imgSrc.isEmpty()) imgSrc = img.attr("data-src");
                        if (!imgSrc.isEmpty() && !imgSrc.contains("data:image")) {
                            imageUrls.add(makeAbsoluteUrl(url, imgSrc));
                        }
                    }
                } else {
                    content = "";
                    contentHtml = "";
                }
                
                // Thumbnail
                Element ogImage = doc.select("meta[property=og:image]").first();
                if (ogImage != null) {
                    thumbnailUrl = ogImage.attr("content");
                } else {
                    Element firstImg = doc.select("article img, .content img").first();
                    if (firstImg != null) {
                        thumbnailUrl = makeAbsoluteUrl(url, firstImg.attr("src"));
                    }
                }
            }
            
            // Nếu không có thumbnail nhưng có ảnh trong bài
            if ((thumbnailUrl == null || thumbnailUrl.isEmpty()) && !imageUrls.isEmpty()) {
                thumbnailUrl = imageUrls.get(0);
            }
            
            if (content.isEmpty() || content.length() < 50) {
                log.warn("⚠️ Content too short or empty");
                return null;
            }
            
            String difficulty = analyzeDifficulty(content);
            String summary = generateSummary(content);
            
            NewsArticle article = NewsArticle.builder()
                .title(title)
                .titleVietnamese("")
                .content(content)           // Plain text cho đọc
                .contentVietnamese("")
                .summary(summary)
                .source(source)
                .sourceUrl(url)
                .difficultyLevel(difficulty)
                .thumbnailUrl(thumbnailUrl)
                .publishedAt(LocalDateTime.now())
                .build();
            
            log.info("✅ Article crawled: {} | {} chars | {} images", title, content.length(), imageUrls.size());
            return article;
            
        } catch (Exception e) {
            log.error("❌ Failed to crawl article from {}: {}", url, e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Extract domain name from URL
     */
    private String extractDomain(String url) {
        try {
            java.net.URL u = new java.net.URL(url);
            String host = u.getHost();
            return host.replaceFirst("^www\\.", "");
        } catch (Exception e) {
            return "Web Article";
        }
    }
    
    /**
     * Make relative URL absolute
     */
    private String makeAbsoluteUrl(String baseUrl, String relativeUrl) {
        if (relativeUrl == null || relativeUrl.isEmpty()) return "";
        if (relativeUrl.startsWith("http")) return relativeUrl;
        if (relativeUrl.startsWith("//")) return "https:" + relativeUrl;
        
        try {
            java.net.URL base = new java.net.URL(baseUrl);
            java.net.URL absolute = new java.net.URL(base, relativeUrl);
            return absolute.toString();
        } catch (Exception e) {
            return relativeUrl;
        }
    }

    /**
     * MANUAL CRAWL API - Gọi thủ công khi cần
     */
    @Transactional
    public int crawlNow(String source, int limit) {
        log.info("🚀 Manual crawl started - Source: {}, Limit: {}", source, limit);
        try {
            List<NewsArticle> articles = switch (source.toLowerCase()) {
                case "voa" -> crawlVOAKorea(limit);
                case "kbs" -> crawlKBSNews(limit);
                default -> {
                    log.warn("⚠️ Unknown source: {}", source);
                    yield new ArrayList<>();
                }
            };

            log.info("💾 Saving {} articles to database...", articles.size());
            articleRepository.saveAll(articles);
            log.info("✅ Crawl completed successfully - {} articles saved", articles.size());
            return articles.size();

        } catch (Exception e) {
            log.error("❌ Manual crawl failed - Source: {}, Error: {}", source, e.getMessage(), e);
            return 0;
        }
    }
}
