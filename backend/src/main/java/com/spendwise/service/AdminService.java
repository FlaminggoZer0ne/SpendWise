package com.spendwise.service;

import com.spendwise.core.BusinessException;
import com.spendwise.dao.CategoryDAO;
import com.spendwise.dao.UserDAO;
import com.spendwise.model.Category;
import com.spendwise.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    private static final int CATEGORY_NAME_MAX = 40;

    private final UserDAO userDAO;
    private final CategoryDAO categoryDAO;

    public AdminService(UserDAO userDAO, CategoryDAO categoryDAO) {
        this.userDAO = userDAO;
        this.categoryDAO = categoryDAO;
    }

    public List<User> listUsers() {
        return userDAO.findAll();
    }

    public boolean setUserActive(int userId, boolean active) {
        User user = userDAO.findById(userId);
        if (user == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Pengguna tidak ditemukan");
        }
        user.setActive(active);
        return userDAO.update(user);
    }

    public boolean createDefaultCategory(Category c) {
        String name = c.getName() == null ? "" : c.getName().trim();
        if (name.isEmpty()) {
            throw new BusinessException("Nama kategori wajib diisi");
        }
        if (name.length() > CATEGORY_NAME_MAX) {
            throw new BusinessException("Nama kategori maksimal 40 karakter");
        }
        if (c.getType() == null) {
            throw new BusinessException("Jenis kategori wajib dipilih");
        }
        c.setName(name);
        c.setDefault(true);
        c.setOwnerId(0);
        boolean duplicate = categoryDAO.findAvailable(0, c.getType()).stream()
                .anyMatch(x -> x.isDefault() && x.getName().equalsIgnoreCase(name));
        if (duplicate) {
            throw new BusinessException(HttpStatus.CONFLICT, "Kategori default dengan nama ini sudah ada");
        }
        return categoryDAO.save(c);
    }

    public boolean deleteDefaultCategory(int categoryId) {
        Category category = categoryDAO.findById(categoryId);
        if (category == null || !category.isDefault()) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Kategori default tidak ditemukan");
        }
        if (categoryDAO.isUsed(categoryId)) {
            throw new BusinessException(HttpStatus.CONFLICT, "Kategori sudah dipakai dan tidak dapat dihapus");
        }
        return categoryDAO.delete(categoryId);
    }
}
