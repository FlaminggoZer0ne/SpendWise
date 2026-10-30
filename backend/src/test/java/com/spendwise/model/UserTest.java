package com.spendwise.model;

import com.spendwise.core.BusinessException;
import com.spendwise.core.PasswordHasher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserTest {

    private Member member() {
        return new Member("budi", PasswordHasher.hash("member12345"), "Budi", "b@mail.com");
    }

    @Test
    void polymorphicRoleAndMenu() {
        User m = member();
        User a = new Admin("admin", "x", "Admin", "a@mail.com", "ADM-001");
        assertEquals("MEMBER", m.getRole());
        assertEquals("ADMIN", a.getRole());
        assertTrue(m.getMenuAccess().contains("WALLETS"));
        assertTrue(a.getMenuAccess().contains("USERS"));
        assertFalse(a.getMenuAccess().contains("WALLETS"));
    }

    @Test
    void checkPassword() {
        User m = member();
        assertTrue(m.checkPassword("member12345"));
        assertFalse(m.checkPassword("salah"));
    }

    @Test
    void changePasswordSucceeds() {
        User m = member();
        assertTrue(m.changePassword("member12345", "rahasia2026"));
        assertTrue(m.checkPassword("rahasia2026"));
        assertFalse(m.checkPassword("member12345"));
    }

    @Test
    void changePasswordRejectsWrongOldShortOrSame() {
        User m = member();
        assertEquals("Password lama tidak sesuai",
                assertThrows(BusinessException.class, () -> m.changePassword("x", "rahasia2026")).getMessage());
        assertThrows(BusinessException.class, () -> m.changePassword("member12345", "pendek"));
        assertThrows(BusinessException.class, () -> m.changePassword("member12345", "member12345"));
    }

    @Test
    void adminPermissions() {
        Admin a = new Admin("admin", "x", "Admin", "a@mail.com", "ADM-001");
        assertTrue(a.canManageUsers());
        assertTrue(a.canManageCategories());
        assertEquals("ADM-001", a.getAdminCode());
    }
}
