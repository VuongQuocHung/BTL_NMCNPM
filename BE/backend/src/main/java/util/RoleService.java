package util;

import dao.RoleDao;
import model.Role;
import util.ApiException;
import java.util.List;

public class RoleService {
    private final RoleDao roleDao;
    public RoleService(RoleDao roleDao) { this.roleDao = roleDao; }

    public List<Role> getAllRoles() { return roleDao.findAll(); }
    public Role getRoleById(Long id) { return roleDao.findById(id).orElseThrow(() -> ApiException.notFound("Role not found")); }
    public Role createRole(Role role) { return roleDao.save(role); }
    public Role updateRole(Long id, Role details) {
        Role role = getRoleById(id);
        role.setName(details.getName());
        return roleDao.merge(role);
    }
    public void deleteRole(Long id) { roleDao.delete(getRoleById(id)); }
}
