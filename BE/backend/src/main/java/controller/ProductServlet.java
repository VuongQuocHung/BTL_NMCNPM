package controller;

import util.ServiceLocator;
import model.Product;
import util.ProductService;
import util.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public class ProductServlet extends BaseServlet {
    private ProductService productService;

    @Override
    public void init() { productService = ServiceLocator.getInstance().productService; }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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
