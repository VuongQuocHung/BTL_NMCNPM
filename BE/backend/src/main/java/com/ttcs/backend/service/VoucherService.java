package com.ttcs.backend.service;

import com.ttcs.backend.entity.Voucher;
import com.ttcs.backend.repository.VoucherRepository;
import com.ttcs.backend.specification.VoucherSpecs;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class VoucherService {
    private final VoucherRepository voucherRepository;

    public Page<Voucher> getFilteredVouchers(String code, Boolean active, Pageable pageable) {
        Specification<Voucher> spec = Specification.where(VoucherSpecs.hasCode(code))
                .and(VoucherSpecs.isActive(active));
        return voucherRepository.findAll(spec, pageable);
    }

    public Page<Voucher> getActiveVouchers(Pageable pageable) {
        return voucherRepository.findAll(VoucherSpecs.isUsableNow(), pageable);
    }

    public Voucher getVoucherById(Long id) {
        return voucherRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Voucher not found"));
    }

    public Voucher createVoucher(Voucher voucher) {
        normalize(voucher);
        if (voucherRepository.existsByCodeIgnoreCase(voucher.getCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Voucher code already exists");
        }
        return voucherRepository.save(voucher);
    }

    public Voucher updateVoucher(Long id, Voucher voucherDetails) {
        Voucher voucher = getVoucherById(id);
        normalize(voucherDetails);
        voucherRepository.findByCodeIgnoreCase(voucherDetails.getCode())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Voucher code already exists");
                });

        voucher.setCode(voucherDetails.getCode());
        voucher.setName(voucherDetails.getName());
        voucher.setDescription(voucherDetails.getDescription());
        voucher.setDiscountType(voucherDetails.getDiscountType());
        voucher.setDiscountValue(voucherDetails.getDiscountValue());
        voucher.setMinOrderAmount(voucherDetails.getMinOrderAmount());
        voucher.setMaxDiscountAmount(voucherDetails.getMaxDiscountAmount());
        voucher.setUsageLimit(voucherDetails.getUsageLimit());
        voucher.setUsedCount(voucherDetails.getUsedCount());
        voucher.setStartDate(voucherDetails.getStartDate());
        voucher.setEndDate(voucherDetails.getEndDate());
        voucher.setActive(voucherDetails.isActive());
        return voucherRepository.save(voucher);
    }

    public void deleteVoucher(Long id) {
        Voucher voucher = getVoucherById(id);
        voucherRepository.delete(voucher);
    }

    private void normalize(Voucher voucher) {
        if (voucher.getCode() == null || voucher.getCode().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Voucher code is required");
        }
        if (voucher.getName() == null || voucher.getName().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Voucher name is required");
        }
        if (voucher.getDiscountType() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Discount type is required");
        }
        if (voucher.getDiscountValue() == null || voucher.getDiscountValue().signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Discount value must be greater than 0");
        }
        if (voucher.getMinOrderAmount() != null && voucher.getMinOrderAmount().signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Minimum order amount must not be negative");
        }
        if (voucher.getMaxDiscountAmount() != null && voucher.getMaxDiscountAmount().signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum discount amount must not be negative");
        }
        if (voucher.getUsageLimit() != null && voucher.getUsageLimit() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usage limit must not be negative");
        }
        if (voucher.getStartDate() != null && voucher.getEndDate() != null
                && voucher.getStartDate().isAfter(voucher.getEndDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date must be before end date");
        }
        if (voucher.getUsedCount() == null || voucher.getUsedCount() < 0) {
            voucher.setUsedCount(0);
        }
        voucher.setCode(voucher.getCode().trim().toUpperCase());
        voucher.setName(voucher.getName().trim());
    }
}
