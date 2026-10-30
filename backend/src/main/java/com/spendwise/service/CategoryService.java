package com.spendwise.service;

import com.spendwise.core.BusinessException;
import com.spendwise.dao.CategoryDAO;
import com.spendwise.model.Category;
import com.spendwise.model.TransactionType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private static final int NAME_MAX = 40;

    private final CategoryDAO categoryDAO;

    public CategoryService(CategoryDAO categoryDAO) {
        this.categoryDAO = categoryDAO;
    }

    /** Kategori default + milik pengguna; {@code type} null berarti semua jenis. */
    public List<Category> getAvailable(int userId, TransactionType type) {
        return categoryDAO.findAvailable(userId, type);
    }

    public boolean createCustom(Category c, int userId) {
        String name = c.getName() == null ? "" : c.getName().trim();
        if (name.isEmpty()) {
            throw new BusinessException("Nama kategori wajib diisi");
        }
        if (name.length() > NAME_MAX) {
            throw new BusinessException("Nama kategori maksimal 40 karakter");
        }
        if (c.getType() == null) {
            throw new BusinessException("Jenis kategori wajib dipilih");
        }
        boolean duplicate = categoryDAO.findAvailable(userId, c.getType()).stream()
                .anyMatch(x -> x.getName().equalsIgnoreCase(name));
        if (duplicate) {
            throw new BusinessException(HttpStatus.CONFLICT, "Nama kategori sudah dipakai");
        }
        c.setName(name);
        c.setDefault(false);
        c.setOwnerId(userId);
        return categoryDAO.save(c);
    }

    public boolean deleteCustom(int id, int userId) {
        Category category = categoryDAO.findById(id);
        if (category == null || (!category.isDefault() && !category.isOwnedBy(userId))) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Kategori tidak ditemukan");
        }
        if (category.isDefault()) {
            throw new BusinessException("Kategori default tidak dapat dihapus");
        }
        if (categoryDAO.isUsed(id)) {
            throw new BusinessException(HttpStatus.CONFLICT, "Kategori sudah dipakai dan tidak dapat dihapus");
        }
        return categoryDAO.delete(id);
    }
}
