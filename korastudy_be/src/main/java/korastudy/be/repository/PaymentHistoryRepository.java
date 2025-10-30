package korastudy.be.repository;

import korastudy.be.entity.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {

    // Lấy danh sách lịch sử thanh toán của 1 user
    List<PaymentHistory> findByUserId(Long userId);

    // Nếu sau này cần kiểm tra giao dịch cụ thể theo user + course
    boolean existsByUserIdAndCourseIdAndTransactionStatus(Long userId, Long courseId, String status);
}
