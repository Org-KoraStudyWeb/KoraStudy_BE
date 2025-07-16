package korastudy.be.repository;

import korastudy.be.entity.Enum.RoleName;
import korastudy.be.entity.User.Account;
import korastudy.be.entity.User.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /*
    ThienTDV - các chức năng liên quan đến account
     */

    //Tìm hồ sơ theo account ID
    Optional<User> findByAccountId(Long accountId);

    boolean existsByUserCode(String UserCode);

    //Tìm user theo account.username (cho /me) **************** Có thể phát triển thêm
    @Query("SELECT u FROM User u WHERE u.account.username = :username")
    Optional<User> findByUsername(@Param("username") String username);

    // Vô hiệu hóa người dùng (soft delete)
    @Modifying
    @Query("UPDATE User u SET u.isEnable = false WHERE u.userCode = :userCode")
    void deleteByUserCode(String UserCode);

    Optional<User> findByAccount(Account account);

    Optional<User> findByAccount_Username(String username);

    Optional<User> findByUserCode(String userCode);

    // Tìm kiếm users
    Page<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrUserCodeContainingIgnoreCase(
            String firstName, String lastName, String email, String userCode, Pageable pageable);

    // Lọc theo role
    Page<User> findByAccount_Roles_RoleName(RoleName roleName, Pageable pageable);

    // Lọc theo status
    Page<User> findByIsEnable(Boolean isEnable, Pageable pageable);

    // Đếm theo role
    long countByAccount_Roles_RoleName(RoleName roleName);

    // Đếm theo status
    long countByIsEnableTrue();
    long countByIsEnableFalse();
}