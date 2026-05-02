package controller;

import util.ServiceLocator;
import model.Role;
import util.RoleService;
import util.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RoleServlet extends BaseServlet {
    private RoleService roleService;
    @Override public void init() { roleService = ServiceLocator.getInstance().roleService; }

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long id = ServletUtil.getPathId(req);
        if (id != null) { JsonUtil.writeJson(resp, 200, roleService.getRoleById(id)); return; }
        JsonUtil.writeJson(resp, 200, roleService.getAllRoles());
    }
    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonUtil.writeJson(resp, 200, roleService.createRole(JsonUtil.readBody(req, Role.class)));
    }
    @Override protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long id = ServletUtil.getPathId(req);
        if (id == null) { JsonUtil.writeError(resp, 400, "Missing ID"); return; }
        JsonUtil.writeJson(resp, 200, roleService.updateRole(id, JsonUtil.readBody(req, Role.class)));
    }
    @Override protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long id = ServletUtil.getPathId(req);
        if (id == null) { JsonUtil.writeError(resp, 400, "Missing ID"); return; }
        roleService.deleteRole(id); resp.setStatus(204);
    }
}
