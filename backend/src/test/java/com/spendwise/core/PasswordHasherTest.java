package com.spendwise.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordHasherTest {

    @Test
    void hashHasSaltDollarSha256Format() {
        String hash = PasswordHasher.hash("rahasia123");
        assertTrue(hash.matches("[0-9a-f]{16}\\$[0-9a-f]{64}"), hash);
    }

    @Test
    void hashUsesRandomSalt() {
        assertNotEquals(PasswordHasher.hash("sama"), PasswordHasher.hash("sama"));
    }

    @Test
    void matchesAcceptsCorrectAndRejectsWrongPassword() {
        String hash = PasswordHasher.hash("rahasia123");
        assertTrue(PasswordHasher.matches("rahasia123", hash));
        assertFalse(PasswordHasher.matches("salah", hash));
    }

    @Test
    void matchesRejectsNullAndMalformedHash() {
        assertFalse(PasswordHasher.matches(null, "a$b"));
        assertFalse(PasswordHasher.matches("x", null));
        assertFalse(PasswordHasher.matches("x", "tanpa-pemisah"));
    }

    @Test
    void matchesSeedAdminPassword() {
        String seed = "a1b2c3d4e5f60718$92cc570e94c7b0c85456ceff697f5daf3680a3f27b56908bf91495a5e070b379";
        assertTrue(PasswordHasher.matches("admin12345", seed));
        assertEquals(16, seed.indexOf('$'));
    }
}
