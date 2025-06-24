package korastudy.be.repository;

import korastudy.be.entity.User.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    /*
    ThienTDV - login, xác thực và phân quyền
     */

    @Query("SELECT account FROM Account account WHERE account.username = ?1")
    Account findByUserName(String userName);

    @Query(value = "INSERT INTO account_roles (account_id, role_id) " + "VALUES (:accountId, :roleId)", nativeQuery = true)
    @Transactional
    @Modifying
    void setRoleForAccount(@Param("accountId") Long accountId, @Param("roleId") Long roleId);
}
