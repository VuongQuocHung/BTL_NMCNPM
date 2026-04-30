package com.ttcs.backend.specification;

import com.ttcs.backend.entity.Voucher;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class VoucherSpecs {
    private VoucherSpecs() {
    }

    public static Specification<Voucher> hasCode(String code) {
        return (root, query, cb) -> {
            if (code == null || code.trim().isEmpty()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("code")), "%" + code.trim().toLowerCase() + "%");
        };
    }

    public static Specification<Voucher> isActive(Boolean active) {
        return (root, query, cb) -> active == null ? cb.conjunction() : cb.equal(root.get("active"), active);
    }

    public static Specification<Voucher> isUsableNow() {
        return (root, query, cb) -> {
            LocalDateTime now = LocalDateTime.now();
            return cb.and(
                    cb.isTrue(root.get("active")),
                    cb.or(cb.isNull(root.get("startDate")), cb.lessThanOrEqualTo(root.get("startDate"), now)),
                    cb.or(cb.isNull(root.get("endDate")), cb.greaterThanOrEqualTo(root.get("endDate"), now)),
                    cb.or(
                            cb.isNull(root.get("usageLimit")),
                            cb.lessThan(root.get("usedCount"), root.get("usageLimit"))
                    )
            );
        };
    }
}
