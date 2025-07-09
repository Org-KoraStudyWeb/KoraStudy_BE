package korastudy.be.entity.User;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import korastudy.be.entity.BaseEntity.BaseTimeEntity;
import korastudy.be.entity.Certificate;
import korastudy.be.entity.Course.CourseTestResult;
import korastudy.be.entity.Course.MyCourse;
import korastudy.be.entity.Enum.Gender;
import korastudy.be.entity.FlashCard.SetCard;
import korastudy.be.entity.MockTest.ComprehensiveTestResult;
import korastudy.be.entity.MockTest.MockTestComment;
import korastudy.be.entity.MockTest.PracticeTestResult;
import korastudy.be.entity.Notification;
import korastudy.be.entity.PaymentHistory;
import korastudy.be.entity.Post.PostComment;
import korastudy.be.entity.Vocabulary.VocabularyProgress;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "users") // Make sure table name is correct
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id") // Explicitly specify column name
    private Long id;

    @Column(unique = true)
    private String userCode;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false, unique = true)
    @JsonManagedReference // 💡 Khi serialize User, nó sẽ mang Account
    private Account account;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(unique = true)
    private String email;

    private String phoneNumber;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "date_of_birth")
    private LocalDate dob;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String level;

    private String idCard;

    private String avatar;

    private boolean isEnable;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MockTestComment> TestComments;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostComment> PostComments;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ComprehensiveTestResult> testResults;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PracticeTestResult> practiceTestResults;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentHistory> paymentHistories;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notification> notifications;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MyCourse> myCourses;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CourseTestResult> courseTestResults;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Certificate> certificates;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SetCard> setCards;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<VocabularyProgress> vocabularyProgresses;

    // Thêm getter cho username nếu chưa có
    public String getUsername() {
        // Nếu có field username thì return username
        // Nếu không có thì tạo từ firstName + lastName
        if (this.firstName != null && this.lastName != null) {
            return this.firstName + " " + this.lastName;
        }
        return this.email; // fallback to email
    }

    // Add toString for debugging
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + getUsername() + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
