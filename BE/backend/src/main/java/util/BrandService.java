package util;

import dao.BrandDao;
import model.Brand;
import util.ApiException;
import java.util.List;

public class BrandService {
    private final BrandDao brandDao;
    public BrandService(BrandDao brandDao) { this.brandDao = brandDao; }

    public List<Brand> getFilteredBrands(String name, String sortBy, String sortDir) {
        return brandDao.findFiltered(name, sortBy, sortDir);
    }

    public Brand getBrandById(Long id) {
        return brandDao.findById(id).orElseThrow(() -> ApiException.notFound("Brand not found"));
    }

    public Brand createBrand(Brand brand) { return brandDao.save(brand); }

    public Brand updateBrand(Long id, Brand details) {
        Brand brand = getBrandById(id);
        brand.setName(details.getName());
        brand.setLogoUrl(details.getLogoUrl());
        return brandDao.merge(brand);
    }

    public void deleteBrand(Long id) { brandDao.delete(getBrandById(id)); }
}
