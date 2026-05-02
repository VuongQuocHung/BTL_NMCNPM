package controller;

import util.ServiceLocator;
import model.Category;
import util.CategoryService;
import util.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CategoryServlet extends BaseServlet {
    private CategoryService categoryService;
    @Override public void init() { categoryService = ServiceLocator.getInstance().categoryService; }

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long id = ServletUtil.getPathId(req);
        if (id != null) { JsonUtil.writeJson(resp, 200, categoryService.getCategoryById(id)); return; }
        String name = ServletUtil.getStringParam(req, "name");
        int page = ServletUtil.getIntParam(req, "page", 0);
        int size = ServletUtil.getIntParam(req, "size", 10);
        JsonUtil.writeJson(resp, 200, PageResponse.of(categoryService.getFilteredCategories(name, "id", "asc"), page, size));
    }
    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonUtil.writeJson(resp, 200, categoryService.createCategory(JsonUtil.readBody(req, Category.class)));
    }
    @Override protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long id = ServletUtil.getPathId(req);
        if (id == null) { JsonUtil.writeError(resp, 400, "Missing ID"); return; }
        JsonUtil.writeJson(resp, 200, categoryService.updateCategory(id, JsonUtil.readBody(req, Category.class)));
    }
    @Override protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long id = ServletUtil.getPathId(req);
        if (id == null) { JsonUtil.writeError(resp, 400, "Missing ID"); return; }
        categoryService.deleteCategory(id); resp.setStatus(204);
    }
}
