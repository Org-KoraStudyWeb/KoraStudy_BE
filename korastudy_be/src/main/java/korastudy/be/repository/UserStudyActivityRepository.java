package korastudy.be.repository;

import korastudy.be.entity.User.User;
import korastudy.be.entity.User.UserStudyActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserStudyActivityRepository extends JpaRepository<UserStudyActivity, Long> {

    Optional<UserStudyActivity> findByUserAndStudyDate(User user, LocalDate studyDate);

    List<UserStudyActivity> findByUserOrderByStudyDateDesc(User user);

    @Query("SELECT COALESCE(SUM(u.studyDurationMinutes), 0) FROM UserStudyActivity u WHERE u.user = :user")
    Integer getTotalStudyMinutes(@Param("user") User user);

    @Query("SELECT u FROM UserStudyActivity u WHERE u.user = :user AND u.studyDate >= :startDate ORDER BY u.studyDate DESC")
    List<UserStudyActivity> findRecentActivities(@Param("user") User user, @Param("startDate") LocalDate startDate);
}
