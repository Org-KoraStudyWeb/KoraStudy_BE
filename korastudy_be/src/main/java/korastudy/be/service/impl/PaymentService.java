package korastudy.be.service.impl;

import korastudy.be.entity.Course.Course;
import korastudy.be.entity.Course.Enrollment;
import korastudy.be.entity.PaymentHistory;
import korastudy.be.entity.User.User;
import korastudy.be.repository.CourseRepository;
import korastudy.be.repository.EnrollmentRepository;
import korastudy.be.repository.PaymentHistoryRepository;
import korastudy.be.repository.UserRepository;
import korastudy.be.service.IEmailService;
import korastudy.be.service.IPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService implements IPaymentService {

    private final PaymentHistoryRepository paymentHistoryRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final IEmailService emailService;

    @Override
    @Transactional
    public PaymentHistory createPayment(Long userId, Long courseId, Double amount) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new RuntimeException("Course not found"));

        PaymentHistory payment = PaymentHistory.builder().user(user).course(course).transactionPrice(amount).transactionStatus("PENDING").dateTransaction(LocalDateTime.now()).build();

        return paymentHistoryRepository.save(payment);
    }

    @Override
    @Transactional
    public PaymentHistory markAsPaid(Long paymentId) {
        PaymentHistory payment = paymentHistoryRepository.findById(paymentId).orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setTransactionStatus("SUCCESS");
        payment.setDateTransaction(LocalDateTime.now());
        paymentHistoryRepository.save(payment);

        Optional<Enrollment> existing = enrollmentRepository.findByUserIdAndCourseId(payment.getUser().getId(), payment.getCourse().getId());

        if (existing.isEmpty()) {
            Enrollment enrollment = Enrollment.builder().user(payment.getUser()).course(payment.getCourse()).enrollDate(LocalDate.now()).expiryDate(LocalDate.now().plusMonths(6)).progress(0.0).build();
            enrollmentRepository.save(enrollment);
        }

        // ✅ Gửi email xác nhận cho user
        emailService.sendPaymentConfirmation(payment.getUser().getAccount(), payment.getCourse(), payment.getTransactionPrice().intValue());

        return payment;
    }

    @Override
    @Transactional
    public PaymentHistory cancelPayment(Long paymentId) {
        PaymentHistory payment = paymentHistoryRepository.findById(paymentId).orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setTransactionStatus("CANCELLED");
        return paymentHistoryRepository.save(payment);
    }

    @Override
    public List<PaymentHistory> getPaymentHistoryByUser(Long userId) {
        return paymentHistoryRepository.findByUserId(userId);
    }

    @Override
    public String getPaymentStatus(Long paymentId) {
        return paymentHistoryRepository.findById(paymentId).map(PaymentHistory::getTransactionStatus).orElse("NOT_FOUND");
    }
}

