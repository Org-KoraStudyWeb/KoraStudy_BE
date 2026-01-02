package korastudy.be.repository.blog;

import korastudy.be.entity.Enum.ReportStatus;
import korastudy.be.entity.Post.PostReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostReportRepository extends JpaRepository<PostReport, Long> {
    
    List<PostReport> findByStatus(ReportStatus status);
    
    List<PostReport> findByPostIdOrderByCreatedAtDesc(Long postId);
    
    @Query("SELECT pr FROM PostReport pr WHERE pr.reporter.id = :userId ORDER BY pr.createdAt DESC")
    List<PostReport> findByReporterId(Long userId);
    
    @Query("SELECT pr FROM PostReport pr WHERE pr.post.id = :postId AND pr.reporter.id = :userId")
    Optional<PostReport> findByPostIdAndReporterId(Long postId, Long userId);
    
    @Query("SELECT COUNT(pr) FROM PostReport pr WHERE pr.post.id = :postId AND pr.status = 'PENDING'")
    Long countPendingReportsByPostId(Long postId);
}
