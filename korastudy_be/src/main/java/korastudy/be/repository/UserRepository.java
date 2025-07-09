package korastudy.be.repository;

import korastudy.be.entity.User.Account;
import korastudy.be.entity.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find user by account ID (method name-based)
    Optional<User> findByAccountId(Long accountId);

    boolean existsByUserCode(String UserCode);

    // Find user by account.username
    @Query("SELECT u FROM User u WHERE u.account.username = :username")
    Optional<User> findByUsername(@Param("username") String username);

    // Soft delete user by userCode
    @Modifying
    @Query("UPDATE User u SET u.isEnable = false WHERE u.userCode = :userCode")
    void deleteByUserCode(String UserCode);

    Optional<User> findByAccount(Account account);

    Optional<User> findByAccount_Username(String username);

    Optional<User> findByUserCode(String userCode);

    // Debug: find by id
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdCustom(@Param("id") Long id);

    @Query("SELECT u FROM User u")
    List<User> findAllUsers();

    // Count total users for debugging
    @Query("SELECT COUNT(u) FROM User u")
    Long countAllUsers();

    // Find user by email
    Optional<User> findByEmail(String email);

    // Debug: get all users with account info
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.account")
    List<User> findAllWithAccount();
}