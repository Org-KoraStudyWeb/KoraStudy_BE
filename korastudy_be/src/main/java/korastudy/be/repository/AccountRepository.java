package korastudy.be.repository;

import korastudy.be.entity.Enum.RoleName;
import korastudy.be.entity.User.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    /*
    ThienTDV - tìm kiếm tài khoản và xác minh
     */

    //Tìm tất cả theo email (dùng cho forgot-password/ xác minh)
    Optional<Account> findByEmail(String email);

    //Tìm tất cả theo username
    Optional<Account> findByUsername(String username);

    //Tìm tất cả account có role cụ thể (cho admin)
    @Query("SELECT a FROM Account a JOIN a.roles r WHERE r.roleName = :roleName")
    List<Account> findAllByRole(@Param("roleName") String roleName);

    //Check Tồn tại email hay username
    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    // Check unique constraints với exclusion
    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByUsernameAndIdNot(String username, Long id);

    /*
    ThienTDV - login, xác thực và phân quyền
     */

    // Tìm tài khoản đang hoạt động (cho login)
    @Query("SELECT a FROM Account a WHERE a.username = :username AND a.isEnabled = true ")
    Optional<Account> findActiveAccountByUsername(@Param("username") String username);

    //Lấy tất cả theo role name
    @Query("SELECT a FROM Account a JOIN a.roles r WHERE r.roleName = :roleName")
    List<Account> findAllByRoles_RoleName(@Param("roleName") RoleName roleName);

    //Set role cho account
    @Modifying
    @Query(value = "INSERT INTO account_roles (account_id, role_id) VALUES (:accountId, :roleId)", nativeQuery = true)
    void setRoleForAcount(@Param("accountId") Long accountId, @Param("roleId") Long roleId);

    /*
    ThienTDV - Đổi mật khẩu or Vô hiệu hóa tài khoản
     */

    // Đổi mật khẩu (nên gọi từ Service với account.setEncryptedPassword(...) + save)
    @Modifying
    @Query("UPDATE Account a SET a.encryptedPassword = :newPassword WHERE a.username = :username AND a.isEnabled = true")
    void updatePassword(@Param("username") String username, @Param("newPassword") String newPassword);

    //Vô hiệu hóa tài khoản (soft delete)
    @Modifying
    @Query("UPDATE Account a SET a.isEnabled = false WHERE a.id = :accountId")
    void disableAccount(@Param("accountId") Long accountId);

    // Đếm theo role
    long countByRoles_RoleName(RoleName roleName);

    // Tìm tài khoản theo username
    @Query("SELECT a FROM Account a WHERE a.username = :username")
    Optional<Account> findAccountByUsername(@Param("username") String username);

    //Xác thực email
    Optional<Account> findByEmailVerificationToken(String token);

    // Methods quên mật khẩu
    Optional<Account> findByPasswordResetToken(String token);

}