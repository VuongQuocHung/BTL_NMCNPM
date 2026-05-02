package controller;

import util.ServiceLocator;
import model.Voucher;
import util.VoucherService;
import util.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class VoucherServlet extends BaseServlet {
    private VoucherService voucherService;
    @Override public void init() { voucherService = ServiceLocator.getInstance().voucherService; }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = ServletUtil.getPathInfo(req);
        if ("/active".equals(path)) {
            int page = ServletUtil.getIntParam(req, "page", 0);
            int size = ServletUtil.getIntParam(req, "size", 20);
            String sortBy = ServletUtil.getStringParam(req, "sortBy");
            String sortDir = ServletUtil.getStringParam(req, "sortDir");
            JsonUtil.writeJson(resp, 200, PageResponse.of(voucherService.getActiveVouchers(sortBy, sortDir), page, size));
            return;
        }
        Long id = ServletUtil.getPathId(req);
        if (id != null) { JsonUtil.writeJson(resp, 200, voucherService.getVoucherById(id)); return; }
        String code = ServletUtil.getStringParam(req, "code");
        String activeStr = ServletUtil.getStringParam(req, "active");
        Boolean active = activeStr != null ? Boolean.parseBoolean(activeStr) : null;
        int page = ServletUtil.getIntParam(req, "page", 0);
        int size = ServletUtil.getIntParam(req, "size", 10);
        String sortBy = ServletUtil.getStringParam(req, "sortBy");
        String sortDir = ServletUtil.getStringParam(req, "sortDir");
        JsonUtil.writeJson(resp, 200, PageResponse.of(voucherService.getFilteredVouchers(code, active, sortBy, sortDir), page, size));
    }

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonUtil.writeJson(resp, 200, voucherService.createVoucher(JsonUtil.readBody(req, Voucher.class)));
    }
    @Override protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long id = ServletUtil.getPathId(req);
        if (id == null) { JsonUtil.writeError(resp, 400, "Missing ID"); return; }
        JsonUtil.writeJson(resp, 200, voucherService.updateVoucher(id, JsonUtil.readBody(req, Voucher.class)));
    }
    @Override protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long id = ServletUtil.getPathId(req);
        if (id == null) { JsonUtil.writeError(resp, 400, "Missing ID"); return; }
        voucherService.deleteVoucher(id); resp.setStatus(204);
    }
}
