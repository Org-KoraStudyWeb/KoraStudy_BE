package korastudy.be.service.impl;

import korastudy.be.dto.request.payment.PaymentRequest;
import korastudy.be.dto.response.payment.PaymentResponse;
import korastudy.be.entity.Course.Course;
import korastudy.be.entity.Course.Enrollment;
import korastudy.be.entity.PaymentHistory;
import korastudy.be.entity.User.Account;
import korastudy.be.entity.User.User;
import korastudy.be.repository.*;
import korastudy.be.service.IEmailService;
import korastudy.be.service.IPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService implements IPaymentService {

    private final PaymentHistoryRepository paymentHistoryRepository;
    private final AccountRepository accountRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final IEmailService emailService;

    @Override
    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        try {
            // Xác thực người dùng
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || authentication.getName() == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED: Missing or invalid authentication token. Please log in again.");
            }

            // Lấy thông tin tài khoản từ authentication
            String username = authentication.getName();
            Account account = accountRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Account not found"));
            User user = account.getUser();

            // Tìm khóa học theo ID
            Course course = courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new RuntimeException("Course not found"));

            // Tạo mã giao dịch duy nhất
            String transactionCode = java.util.UUID.randomUUID().toString().replace("-", "");

            // Tạo lịch sử thanh toán với thông tin từ request
            PaymentHistory payment = PaymentHistory.builder()
                    .user(user)
                    .course(course)
                    .transactionPrice(request.getAmount())
                    .transactionStatus("PENDING")
                    .transactionCode(transactionCode)
                    .buyerName(request.getBuyerName())
                    .buyerEmail(request.getBuyerEmail())
                    .buyerPhone(request.getBuyerPhone())
                    .paymentMethod("VNPAY")
                    .dateTransaction(LocalDateTime.now())
                    .build();

            // Lưu thông tin thanh toán
            paymentHistoryRepository.save(payment);

            return mapToResponse(payment);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    @Transactional
    public PaymentResponse markAsPaid(Long paymentId) {
        try {
            // Tìm thanh toán theo ID
            PaymentHistory payment = paymentHistoryRepository.findById(paymentId)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            // Cập nhật trạng thái thanh toán thành công
            payment.setTransactionStatus("SUCCESS");
            payment.setDateTransaction(LocalDateTime.now());
            paymentHistoryRepository.save(payment);

            // Kiểm tra xem user đã đăng ký khóa học chưa
            Optional<Enrollment> existing = enrollmentRepository.findByUserIdAndCourseId(
                    payment.getUser().getId(),
                    payment.getCourse().getId()
            );

            // Nếu chưa đăng ký thì tạo mới enrollment
            if (existing.isEmpty()) {
                Enrollment enrollment = Enrollment.builder()
                        .user(payment.getUser())
                        .course(payment.getCourse())
                        .enrollDate(LocalDate.now())
                        .expiryDate(LocalDate.now().plusMonths(6))
                        .progress(0.0)
                        .build();
                enrollmentRepository.save(enrollment);
            }

            // Gửi email xác nhận thanh toán
            try {
                emailService.sendPaymentConfirmation(
                        payment.getUser().getAccount(),
                        payment.getCourse(),
                        payment.getTransactionPrice().intValue()
                );
            } catch (Exception e) {
                // Bỏ qua lỗi gửi email, không ảnh hưởng đến thanh toán
            }

            return mapToResponse(payment);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    @Transactional
    public PaymentResponse cancelPayment(Long paymentId) {
        try {
            // Tìm thanh toán theo ID
            PaymentHistory payment = paymentHistoryRepository.findById(paymentId)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            // Cập nhật trạng thái thành đã hủy
            payment.setTransactionStatus("CANCELLED");
            paymentHistoryRepository.save(payment);

            return mapToResponse(payment);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public List<PaymentResponse> getPaymentHistoryByUser(Long userId) {
        // Lấy lịch sử thanh toán của user
        return paymentHistoryRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public String getPaymentStatus(Long paymentId) {
        // Lấy trạng thái thanh toán
        return paymentHistoryRepository.findById(paymentId)
                .map(PaymentHistory::getTransactionStatus)
                .orElse("NOT_FOUND");
    }

    // Chuyển đổi entity sang DTO
    private PaymentResponse mapToResponse(PaymentHistory payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getTransactionPrice(),
                payment.getTransactionStatus(),
                payment.getTransactionCode(),
                payment.getBuyerName(),
                payment.getBuyerEmail(),
                payment.getBuyerPhone(),
                payment.getUser().getId(),
                payment.getCourse().getId()
        );
    }
}