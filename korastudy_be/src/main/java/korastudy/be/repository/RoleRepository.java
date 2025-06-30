package korastudy.be.repository;

import korastudy.be.entity.Enum.RoleName;
import korastudy.be.entity.User.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {


    Optional<Role> findByRoleName(RoleName name);

    List<Role> findByRoleNameIn(List<RoleName> names);

}
