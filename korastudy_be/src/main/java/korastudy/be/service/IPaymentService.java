package korastudy.be.service;

import korastudy.be.dto.request.payment.PaymentRequest;
import korastudy.be.dto.response.payment.PaymentDetailResponse;
import korastudy.be.dto.response.payment.PaymentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface IPaymentService {

    // Tạo payment mới, trả về DTO
    PaymentResponse createPayment(PaymentRequest request);

    // Cập nhật trạng thái thành SUCCESS
    PaymentResponse markAsPaid(Long paymentId);

    // Hủy payment
    PaymentResponse cancelPayment(Long paymentId);

    // Lấy lịch sử payment của user theo userId
    List<PaymentResponse> getPaymentHistoryByUser(Long userId);

    // Lấy trạng thái payment
    String getPaymentStatus(Long paymentId);

    /*
     * Lấy danh sách người dùng đã mua khóa học cho admin
     */

    Page<PaymentDetailResponse> getAllPaymentsForAdmin(Pageable pageable);

    PaymentDetailResponse getPaymentDetailsForAdmin(Long paymentId);

    /*
     * TÌM KIẾM & LỌC - Quan trọng nhất
     */
    // Tìm kiếm tổng hợp theo nhiều trường
    Page<PaymentDetailResponse> searchPayments(
            String keyword, // Tìm theo: buyerName, buyerEmail, buyerPhone, transactionCode
            String status, // Lọc theo trạng thái
            String paymentMethod, // Lọc theo phương thức
            LocalDateTime fromDate, // Từ ngày
            LocalDateTime toDate, // Đến ngày
            Pageable pageable // Sort và phân trang
    );

    /*
     * THỐNG KÊ ĐƠN GIẢN
     */
    // Tổng doanh thu
    Double getTotalRevenue();

    // Đếm số lượng theo trạng thái
    Map<String, Long> countByStatus();

    /*
     * XUẤT FILE
     */
    // Xuất ra PDF
    byte[] exportToPdf(LocalDateTime fromDate, LocalDateTime toDate);

    // Xuất ra Excel
    byte[] exportToExcel(LocalDateTime fromDate, LocalDateTime toDate);
}
