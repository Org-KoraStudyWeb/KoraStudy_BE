package korastudy.be.service;

import korastudy.be.entity.Enum.RoleName;
import korastudy.be.entity.User.Role;

import java.util.List;

public interface IRoleService {
    
    List<Role> getAllRoles();

    Role findById(Long id);

    List<Role> getManagerRoles();

    Role getRoleByName(RoleName roleName);
}
