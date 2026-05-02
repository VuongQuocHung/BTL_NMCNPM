package controller;

import model.dto.ApplyVoucherRequest;
import model.dto.CartItemRequest;
import model.dto.UpdateCartItemRequest;
import util.ServiceLocator;
import util.CartService;
import util.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CartServlet extends BaseServlet {
    private CartService cartService;
    @Override public void init() { cartService = ServiceLocator.getInstance().cartService; }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long userId = ServletUtil.getCurrentUserId(req);
        JsonUtil.writeJson(resp, 200, cartService.getCart(userId));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long userId = ServletUtil.getCurrentUserId(req);
        String path = ServletUtil.getPathInfo(req);
        if ("/items".equals(path)) {
            CartItemRequest body = JsonUtil.readBody(req, CartItemRequest.class);
            JsonUtil.writeJson(resp, 200, cartService.addItem(userId, body.getProductId(), body.getQuantity()));
        } else if ("/voucher".equals(path)) {
            ApplyVoucherRequest body = JsonUtil.readBody(req, ApplyVoucherRequest.class);
            JsonUtil.writeJson(resp, 200, cartService.applyVoucher(userId, body.getCode()));
        } else {
            JsonUtil.writeError(resp, 404, "Not found");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long userId = ServletUtil.getCurrentUserId(req);
        String path = ServletUtil.getPathInfo(req);
        // /items/{productId}
        if (path != null && path.startsWith("/items/")) {
            String idStr = path.substring("/items/".length());
            Long productId = Long.parseLong(idStr);
            UpdateCartItemRequest body = JsonUtil.readBody(req, UpdateCartItemRequest.class);
            JsonUtil.writeJson(resp, 200, cartService.updateItem(userId, productId, body.getQuantity()));
        } else {
            JsonUtil.writeError(resp, 404, "Not found");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long userId = ServletUtil.getCurrentUserId(req);
        String path = ServletUtil.getPathInfo(req);
        if ("/items".equals(path)) {
            JsonUtil.writeJson(resp, 200, cartService.clearCart(userId));
        } else if (path != null && path.startsWith("/items/")) {
            Long productId = Long.parseLong(path.substring("/items/".length()));
            JsonUtil.writeJson(resp, 200, cartService.removeItem(userId, productId));
        } else if ("/voucher".equals(path)) {
            JsonUtil.writeJson(resp, 200, cartService.removeVoucher(userId));
        } else {
            JsonUtil.writeError(resp, 404, "Not found");
        }
    }
}
