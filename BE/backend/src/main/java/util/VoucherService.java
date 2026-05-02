package util;

import dao.VoucherDao;
import model.Voucher;
import util.ApiException;
import java.util.List;

public class VoucherService {
    private final VoucherDao voucherDao;
    public VoucherService(VoucherDao voucherDao) { this.voucherDao = voucherDao; }

    public List<Voucher> getFilteredVouchers(String code, Boolean active, String sortBy, String sortDir) {
        return voucherDao.findFiltered(code, active, sortBy, sortDir);
    }

    public List<Voucher> getActiveVouchers(String sortBy, String sortDir) {
        return voucherDao.findActiveVouchers(sortBy, sortDir);
    }

    public Voucher getVoucherById(Long id) {
        return voucherDao.findById(id).orElseThrow(() -> ApiException.notFound("Voucher not found"));
    }

    public Voucher createVoucher(Voucher voucher) {
        normalize(voucher);
        if (voucherDao.existsByCodeIgnoreCase(voucher.getCode())) throw ApiException.badRequest("Voucher code already exists");
        return voucherDao.save(voucher);
    }

    public Voucher updateVoucher(Long id, Voucher details) {
        Voucher voucher = getVoucherById(id);
        normalize(details);
        voucherDao.findByCodeIgnoreCase(details.getCode())
                .filter(e -> !e.getId().equals(id))
                .ifPresent(e -> { throw ApiException.badRequest("Voucher code already exists"); });
        voucher.setCode(details.getCode()); voucher.setName(details.getName());
        voucher.setDescription(details.getDescription()); voucher.setDiscountType(details.getDiscountType());
        voucher.setDiscountValue(details.getDiscountValue()); voucher.setMinOrderAmount(details.getMinOrderAmount());
        voucher.setMaxDiscountAmount(details.getMaxDiscountAmount()); voucher.setUsageLimit(details.getUsageLimit());
        voucher.setUsedCount(details.getUsedCount()); voucher.setStartDate(details.getStartDate());
        voucher.setEndDate(details.getEndDate()); voucher.setActive(details.isActive());
        return voucherDao.merge(voucher);
    }

    public void deleteVoucher(Long id) { voucherDao.delete(getVoucherById(id)); }

    private void normalize(Voucher v) {
        if (v.getCode() == null || v.getCode().isBlank()) throw ApiException.badRequest("Voucher code is required");
        if (v.getName() == null || v.getName().isBlank()) throw ApiException.badRequest("Voucher name is required");
        if (v.getDiscountType() == null) throw ApiException.badRequest("Discount type is required");
        if (v.getDiscountValue() == null || v.getDiscountValue().signum() <= 0) throw ApiException.badRequest("Discount value must be > 0");
        if (v.getUsedCount() == null || v.getUsedCount() < 0) v.setUsedCount(0);
        v.setCode(v.getCode().trim().toUpperCase());
        v.setName(v.getName().trim());
    }
}
