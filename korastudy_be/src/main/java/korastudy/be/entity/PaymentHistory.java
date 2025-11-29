package korastudy.be.entity;

import jakarta.persistence.*;
import korastudy.be.entity.BaseEntity.BaseTimeEntity;
import korastudy.be.entity.Course.Course;
import korastudy.be.entity.User.User;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "payment_history")
public class PaymentHistory extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_transaction", nullable = false)
    private LocalDateTime dateTransaction;

    @Column(name = "transaction_price", nullable = false)
    private Double transactionPrice;

    @Column(name = "transaction_status", nullable = false)
    private String transactionStatus;

    @Column(name = "payment_method")
    private String paymentMethod; // MOMO, ZaloPay, VNPAY...

    @Column(name = "transaction_code")
    private String transactionCode; // Mã giao dịch từ cổng thanh toán

    @Column(name = "buyer_name", columnDefinition = "NVARCHAR(255)")
    private String buyerName;

    @Column(name = "buyer_email")
    private String buyerEmail;

    @Column(name = "buyer_phone")
    private String buyerPhone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
}
