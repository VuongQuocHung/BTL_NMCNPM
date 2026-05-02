package controller;

import util.ServiceLocator;
import model.Brand;
import util.BrandService;
import util.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class BrandServlet extends BaseServlet {
    private BrandService brandService;
    @Override public void init() { brandService = ServiceLocator.getInstance().brandService; }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long id = ServletUtil.getPathId(req);
        if (id != null) { JsonUtil.writeJson(resp, 200, brandService.getBrandById(id)); return; }
        String name = ServletUtil.getStringParam(req, "name");
        int page = ServletUtil.getIntParam(req, "page", 0);
        int size = ServletUtil.getIntParam(req, "size", 10);
        JsonUtil.writeJson(resp, 200, PageResponse.of(brandService.getFilteredBrands(name, "id", "asc"), page, size));
    }

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonUtil.writeJson(resp, 200, brandService.createBrand(JsonUtil.readBody(req, Brand.class)));
    }
    @Override protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long id = ServletUtil.getPathId(req);
        if (id == null) { JsonUtil.writeError(resp, 400, "Missing ID"); return; }
        JsonUtil.writeJson(resp, 200, brandService.updateBrand(id, JsonUtil.readBody(req, Brand.class)));
    }
    @Override protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long id = ServletUtil.getPathId(req);
        if (id == null) { JsonUtil.writeError(resp, 400, "Missing ID"); return; }
        brandService.deleteBrand(id); resp.setStatus(204);
    }
}
