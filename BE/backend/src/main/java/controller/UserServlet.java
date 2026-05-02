package controller;

import util.ServiceLocator;
import model.User;
import util.UserService;
import util.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class UserServlet extends BaseServlet {
    private UserService userService;
    @Override public void init() { userService = ServiceLocator.getInstance().userService; }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = ServletUtil.getPathInfo(req);
        if ("/me".equals(path)) {
            requireAuth(req);
            JsonUtil.writeJson(resp, 200, userService.getUserProfile(ServletUtil.getCurrentUserId(req)));
            return;
        }
        Long id = ServletUtil.getPathId(req);
        if (id != null) { JsonUtil.writeJson(resp, 200, userService.getUserById(id)); return; }
        int page = ServletUtil.getIntParam(req, "page", 0);
        int size = ServletUtil.getIntParam(req, "size", 10);
        String sortBy = ServletUtil.getStringParam(req, "sortBy");
        String sortDir = ServletUtil.getStringParam(req, "sortDir");
        JsonUtil.writeJson(resp, 200, PageResponse.of(
                userService.getFilteredUsers(
                        ServletUtil.getStringParam(req, "email"), ServletUtil.getStringParam(req, "fullName"),
                        ServletUtil.getStringParam(req, "phone"), ServletUtil.getLongParam(req, "roleId"),
                        page, size, sortBy != null ? sortBy : "id", sortDir != null ? sortDir : "asc"), page, size));
    }

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonUtil.writeJson(resp, 200, userService.createUser(JsonUtil.readBody(req, User.class)));
    }
    @Override protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long id = ServletUtil.getPathId(req);
        if (id == null) { JsonUtil.writeError(resp, 400, "Missing ID"); return; }
        JsonUtil.writeJson(resp, 200, userService.updateUser(id, JsonUtil.readBody(req, User.class)));
    }
    @Override protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long id = ServletUtil.getPathId(req);
        if (id == null) { JsonUtil.writeError(resp, 400, "Missing ID"); return; }
        userService.deleteUser(id); resp.setStatus(204);
    }
}
