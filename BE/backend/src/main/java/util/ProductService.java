package util;

import dao.ProductDao;
import model.Product;
import model.ProductImage;
import model.ProductSpecification;
import util.ApiException;
import java.math.BigDecimal;
import java.util.List;

public class ProductService {
    private final ProductDao productDao;
    public ProductService(ProductDao productDao) { this.productDao = productDao; }

    public List<Product> getFilteredProducts(String name, Long brandId, Long categoryId,
                                              BigDecimal minPrice, BigDecimal maxPrice,
                                              int page, int size, String sortBy, String sortDir) {
        return productDao.findFiltered(name, brandId, categoryId, minPrice, maxPrice, page, size, sortBy, sortDir);
    }

    public Product getProductById(Long id) {
        return productDao.findWithDetailsById(id)
                .orElseThrow(() -> ApiException.notFound("Product not found"));
    }

    public List<Product> getProductsByIds(List<Long> ids) {
        return productDao.findByIdsWithDetails(ids);
    }

    public Product createProduct(Product product) {
        if (product.getImages() != null) product.getImages().forEach(img -> img.setProduct(product));
        if (product.getSpecification() != null) product.getSpecification().setProduct(product);
        return productDao.save(product);
    }

    public Product updateProduct(Long id, Product details) {
        Product product = getProductById(id);
        product.setName(details.getName());
        product.setPrice(details.getPrice());
        product.setImportPrice(details.getImportPrice());
        product.setStock(details.getStock());
        product.setDescription(details.getDescription());
        product.setBrand(details.getBrand());
        product.setCategory(details.getCategory());
        if (details.getImages() != null && !details.getImages().isEmpty()) {
            product.getImages().clear();
            for (ProductImage img : details.getImages()) { img.setProduct(product); product.getImages().add(img); }
        }
        if (details.getSpecification() != null) {
            ProductSpecification spec = product.getSpecification();
            ProductSpecification newSpec = details.getSpecification();
            if (spec == null) { newSpec.setProduct(product); product.setSpecification(newSpec); }
            else {
                spec.setCpu(newSpec.getCpu()); spec.setRam(newSpec.getRam());
                spec.setStorage(newSpec.getStorage()); spec.setVga(newSpec.getVga());
                spec.setScreen(newSpec.getScreen()); spec.setOs(newSpec.getOs());
                spec.setBattery(newSpec.getBattery()); spec.setWeight(newSpec.getWeight());
            }
        }
        return productDao.merge(product);
    }

    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        productDao.delete(product);
    }
}
