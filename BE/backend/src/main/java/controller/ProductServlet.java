package controller;

import util.ServiceLocator;
import model.Product;
import util.ProductService;
import util.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ProductServlet extends BaseServlet {
    private ProductService productService;

    @Override
    public void init() { productService = ServiceLocator.getInstance().productService; }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = ServletUtil.getPathInfo(req);
        if ("/compare".equals(path)) {
            handleCompare(req, resp);
            return;
        }
        Long id = ServletUtil.getPathId(req);
        if (id != null) {
            JsonUtil.writeJson(resp, 200, productService.getProductById(id));
        } else {
            String name = ServletUtil.getStringParam(req, "name");
            Long brandId = ServletUtil.getLongParam(req, "brandId");
            Long categoryId = ServletUtil.getLongParam(req, "categoryId");
            String minPriceStr = ServletUtil.getStringParam(req, "minPrice");
            String maxPriceStr = ServletUtil.getStringParam(req, "maxPrice");
            BigDecimal minPrice = minPriceStr != null ? new BigDecimal(minPriceStr) : null;
            BigDecimal maxPrice = maxPriceStr != null ? new BigDecimal(maxPriceStr) : null;
            int page = ServletUtil.getIntParam(req, "page", 0);
            int size = ServletUtil.getIntParam(req, "size", 10);
            String sortBy = ServletUtil.getStringParam(req, "sortBy");
            String sortDir = ServletUtil.getStringParam(req, "sortDir");
            List<Product> all = productService.getFilteredProducts(name, brandId, categoryId, minPrice, maxPrice, page, size, sortBy, sortDir);
            JsonUtil.writeJson(resp, 200, PageResponse.of(all, page, size));
        }
    }

    private void handleCompare(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idsParam = ServletUtil.getStringParam(req, "ids");
        if (idsParam == null) {
            JsonUtil.writeError(resp, 400, "Thiếu danh sách sản phẩm so sánh");
            return;
        }
        List<Long> ids = parseIds(idsParam);
        if (ids.size() < 2) {
            JsonUtil.writeError(resp, 400, "Vui lòng chọn ít nhất 2 sản phẩm để so sánh");
            return;
        }
        if (ids.size() > 3) {
            JsonUtil.writeError(resp, 400, "Chỉ được phép so sánh tối đa 3 sản phẩm cùng lúc");
            return;
        }

        List<Product> products = productService.getProductsByIds(ids);
        if (products.size() != ids.size()) {
            JsonUtil.writeError(resp, 404, "Một hoặc nhiều sản phẩm không tồn tại");
            return;
        }

        Long categoryId = products.get(0).getCategory() != null ? products.get(0).getCategory().getId() : null;
        boolean sameCategory = products.stream().allMatch(p -> {
            Long cid = p.getCategory() != null ? p.getCategory().getId() : null;
            return categoryId != null && categoryId.equals(cid);
        });
        if (!sameCategory) {
            JsonUtil.writeError(resp, 400, "Vui lòng chọn các sản phẩm cùng loại để thực hiện so sánh");
            return;
        }

        JsonUtil.writeJson(resp, 200, products);
    }

    private List<Long> parseIds(String idsParam) {
        String[] parts = idsParam.split(",");
        Set<Long> unique = new LinkedHashSet<>();
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) continue;
            try {
                unique.add(Long.parseLong(trimmed));
            } catch (NumberFormatException ignored) {
                // skip invalid ids
            }
        }
        return new ArrayList<>(unique);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Product product = JsonUtil.readBody(req, Product.class);
        JsonUtil.writeJson(resp, 200, productService.createProduct(product));
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long id = ServletUtil.getPathId(req);
        if (id == null) { JsonUtil.writeError(resp, 400, "Missing product ID"); return; }
        Product product = JsonUtil.readBody(req, Product.class);
        JsonUtil.writeJson(resp, 200, productService.updateProduct(id, product));
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long id = ServletUtil.getPathId(req);
        if (id == null) { JsonUtil.writeError(resp, 400, "Missing product ID"); return; }
        productService.deleteProduct(id);
        resp.setStatus(204);
    }
}
