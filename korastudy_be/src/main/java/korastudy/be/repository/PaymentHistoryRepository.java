package korastudy.be.repository;

import korastudy.be.entity.PaymentHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {

    // Lấy danh sách lịch sử thanh toán của 1 user
    List<PaymentHistory> findByUserId(Long userId);

    // Nếu sau này cần kiểm tra giao dịch cụ thể theo user + course
    boolean existsByUserIdAndCourseIdAndTransactionStatus(Long userId, Long courseId, String status);

    // Tìm payment theo transactionCode (vnp_TxnRef)
    Optional<PaymentHistory> findByTransactionCode(String transactionCode);


    @Query("SELECT p FROM PaymentHistory p")
    Page<PaymentHistory> findAllByOrderByDateTransactionDesc(Pageable pageable);

    @Query("SELECT p FROM PaymentHistory p ORDER BY p.dateTransaction DESC")
    List<PaymentHistory> findAllByOrderByDateTransactionDesc();

    // Tìm kiếm theo nhiều tiêu chí
    @Query("SELECT p FROM PaymentHistory p WHERE " +
            "(:keyword IS NULL OR " +
            "LOWER(p.buyerName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.buyerEmail) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.buyerPhone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.transactionCode) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:status IS NULL OR p.transactionStatus = :status) AND " +
            "(:paymentMethod IS NULL OR p.paymentMethod = :paymentMethod) AND " +
            "(:startDate IS NULL OR p.dateTransaction >= :startDate) AND " +
            "(:endDate IS NULL OR p.dateTransaction <= :endDate)")
    Page<PaymentHistory> searchPayments(
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("paymentMethod") String paymentMethod,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    @Query("SELECT COALESCE(SUM(p.transactionPrice), 0) FROM PaymentHistory p WHERE p.transactionStatus = 'SUCCESS'")
    Double calculateTotalRevenue();

    @Query("SELECT p.transactionStatus, COUNT(p) FROM PaymentHistory p GROUP BY p.transactionStatus")
    List<Object[]> countByStatus();

    List<PaymentHistory> findByDateTransactionBetween(LocalDateTime fromDate, LocalDateTime toDate);
}
