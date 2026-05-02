package util;

import dao.BrandDao;
import dao.CategoryDao;
import dao.ProductDao;
import model.Brand;
import model.Category;
import model.Product;
import model.ProductImage;
import model.ProductSpecification;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

public class DataInitializer {

    public static void initSampleProducts() {
        BrandDao brandDao = ServiceLocator.getInstance().brandDao;
        CategoryDao categoryDao = ServiceLocator.getInstance().categoryDao;
        ProductDao productDao = ServiceLocator.getInstance().productDao;

        // 1. Create Brands
        Brand dell = brandDao.findByName("Dell").orElseGet(() -> brandDao.save(Brand.builder().name("Dell").build()));
        Brand apple = brandDao.findByName("Apple").orElseGet(() -> brandDao.save(Brand.builder().name("Apple").build()));
        Brand asus = brandDao.findByName("Asus").orElseGet(() -> brandDao.save(Brand.builder().name("Asus").build()));
        Brand hp = brandDao.findByName("HP").orElseGet(() -> brandDao.save(Brand.builder().name("HP").build()));
        Brand msi = brandDao.findByName("MSI").orElseGet(() -> brandDao.save(Brand.builder().name("MSI").build()));
        Brand acer = brandDao.findByName("Acer").orElseGet(() -> brandDao.save(Brand.builder().name("Acer").build()));

        // 2. Create Categories
        Category gaming = categoryDao.findByName("Gaming").orElseGet(() -> categoryDao.save(Category.builder().name("Gaming").build()));
        Category office = categoryDao.findByName("Office").orElseGet(() -> categoryDao.save(Category.builder().name("Office").build()));
        Category ultra = categoryDao.findByName("Ultra-thin").orElseGet(() -> categoryDao.save(Category.builder().name("Ultra-thin").build()));
        Category workstation = categoryDao.findByName("Workstation").orElseGet(() -> categoryDao.save(Category.builder().name("Workstation").build()));

        // 3. Create Products
        createProductIfNotExist(productDao, "Dell Alienware M15 R7", new BigDecimal("48000000"), new BigDecimal("40000000"), 10, 
                "Laptop gaming mạnh mẽ nhất từ Dell với RTX 3070Ti", dell, gaming, "Intel Core i7-12700H", "16GB", "512GB SSD", "RTX 3070Ti");

        createProductIfNotExist(productDao, "MacBook Pro 14 M3 Pro", new BigDecimal("52000000"), new BigDecimal("45000000"), 5, 
                "Siêu phẩm đồ họa từ Apple với chip M3 Pro", apple, ultra, "Apple M3 Pro", "18GB", "512GB SSD", "Apple GPU 14-core");

        createProductIfNotExist(productDao, "Asus ROG Zephyrus G14", new BigDecimal("35000000"), new BigDecimal("28000000"), 12, 
                "Laptop gaming 14 inch mạnh mẽ nhất thế giới", asus, gaming, "AMD Ryzen 9 7940HS", "16GB", "1TB SSD", "RTX 4060");

        createProductIfNotExist(productDao, "HP Spectre x360", new BigDecimal("38000000"), new BigDecimal("30000000"), 8, 
                "Laptop xoay gập 360 độ sang trọng", hp, ultra, "Intel Core i7-1355U", "16GB", "512GB SSD", "Intel Iris Xe");

        createProductIfNotExist(productDao, "MSI Katana 15", new BigDecimal("25000000"), new BigDecimal("20000000"), 20, 
                "Laptop gaming giá rẻ hiệu năng cao", msi, gaming, "Intel Core i7-13620H", "16GB", "512GB SSD", "RTX 4050");

        createProductIfNotExist(productDao, "Acer Predator Helios Neo 16", new BigDecimal("32000000"), new BigDecimal("26000000"), 15, 
                "Chiến binh gaming mới từ Acer", acer, gaming, "Intel Core i7-13700HX", "16GB", "512GB SSD", "RTX 4060");
        
        createProductIfNotExist(productDao, "Dell XPS 15", new BigDecimal("45000000"), new BigDecimal("38000000"), 6, 
                "Đỉnh cao laptop multimedia", dell, workstation, "Intel Core i9-13900H", "32GB", "1TB SSD", "RTX 4070");
                
        createProductIfNotExist(productDao, "MacBook Air M2", new BigDecimal("26000000"), new BigDecimal("22000000"), 25, 
                "Laptop mỏng nhẹ bán chạy nhất", apple, ultra, "Apple M2", "8GB", "256GB SSD", "Apple GPU 8-core");
    }

    private static void createProductIfNotExist(ProductDao productDao, String name, BigDecimal price, BigDecimal importPrice, 
                                               int stock, String desc, Brand brand, Category category,
                                               String cpu, String ram, String storage, String vga) {
        if (productDao.findAll().stream().noneMatch(p -> p.getName().equalsIgnoreCase(name))) {
            Product product = Product.builder()
                    .name(name)
                    .price(price)
                    .importPrice(importPrice)
                    .stock(stock)
                    .description(desc)
                    .brand(brand)
                    .category(category)
                    .build();

            // Set specification
            ProductSpecification spec = ProductSpecification.builder()
                    .product(product)
                    .cpu(cpu)
                    .ram(ram)
                    .storage(storage)
                    .vga(vga)
                    .screen("15.6 inch")
                    .os("Windows 11")
                    .build();
            product.setSpecification(spec);

            // Set images - Sử dụng ảnh có sẵn trong thư mục uploads của bạn
            Set<ProductImage> images = new HashSet<>();
            images.add(ProductImage.builder().product(product).imageUrl("uploads/1ac80bc6-d6a6-4014-ba7c-da4f131cfe21.jpg").isPrimary(true).build());
            product.setImages(images);

            productDao.save(product);
            System.out.println("Created sample product: " + name);
        }
    }
}
