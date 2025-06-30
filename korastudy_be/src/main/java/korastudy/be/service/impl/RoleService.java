package korastudy.be.service.impl;

import korastudy.be.entity.Enum.RoleName;
import korastudy.be.entity.User.Role;
import korastudy.be.exception.AlreadyExistsException;
import korastudy.be.repository.RoleRepository;
import korastudy.be.service.IRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class RoleService implements IRoleService {
    private final RoleRepository roleRepository;

    @Override
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Override
    public Role findById(Long id) {
        return roleRepository.findById(id).orElseThrow(() -> new AlreadyExistsException("Không tìm thấy role cần thiết"));
    }

    @Override
    public List<Role> getManagerRoles() {
        return roleRepository.findByRoleNameIn(List.of(RoleName.CONTENT_MANAGER, RoleName.DELIVERY_MANAGER));
    }

    @Override
    public Role getRoleByName(RoleName roleName) {
        return roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new AlreadyExistsException("Không tìm thấy quyền: " + roleName));
    }
}
