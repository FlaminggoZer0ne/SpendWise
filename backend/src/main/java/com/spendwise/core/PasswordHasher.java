package com.spendwise.core;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

/** Utilitas hash password berformat {@code salt$sha256hex} (salt = 16 karakter hex). */
public final class PasswordHasher {

    private static final int SALT_BYTES = 8;
    private static final char SEPARATOR = '$';
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHasher() {
    }

    public static String hash(String raw) {
        byte[] saltBytes = new byte[SALT_BYTES];
        RANDOM.nextBytes(saltBytes);
        String salt = HexFormat.of().formatHex(saltBytes);
        return salt + SEPARATOR + sha256Hex(salt + raw);
    }

    public static boolean matches(String raw, String stored) {
        if (raw == null || stored == null) {
            return false;
        }
        int idx = stored.indexOf(SEPARATOR);
        if (idx <= 0) {
            return false;
        }
        String salt = stored.substring(0, idx);
        String expected = stored.substring(idx + 1);
        return MessageDigest.isEqual(
                sha256Hex(salt + raw).getBytes(StandardCharsets.UTF_8),
                expected.getBytes(StandardCharsets.UTF_8));
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(input.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 tidak tersedia", e);
        }
    }
}
