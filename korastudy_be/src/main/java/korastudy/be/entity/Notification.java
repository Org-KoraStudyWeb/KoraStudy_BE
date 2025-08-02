package korastudy.be.entity;

import jakarta.persistence.*;
import korastudy.be.entity.User.User;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    
    private String content;
    
    private boolean read;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        // Mặc định: thông báo tồn tại 7 ngày
        expirationDate = createdAt.plusDays(7);
    }
}