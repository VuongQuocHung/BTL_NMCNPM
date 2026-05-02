package util;

import dao.CategoryDao;
import model.Category;
import util.ApiException;
import java.util.List;

public class CategoryService {
    private final CategoryDao categoryDao;
    public CategoryService(CategoryDao categoryDao) { this.categoryDao = categoryDao; }

    public List<Category> getFilteredCategories(String name, String sortBy, String sortDir) {
        return categoryDao.findFiltered(name, sortBy, sortDir);
    }

    public Category getCategoryById(Long id) {
        return categoryDao.findById(id).orElseThrow(() -> ApiException.notFound("Category not found"));
    }

    public Category createCategory(Category cat) { return categoryDao.save(cat); }

    public Category updateCategory(Long id, Category details) {
        Category cat = getCategoryById(id);
        cat.setName(details.getName());
        cat.setDescription(details.getDescription());
        return categoryDao.merge(cat);
    }

    public void deleteCategory(Long id) { categoryDao.delete(getCategoryById(id)); }
}
