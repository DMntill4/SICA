package com.acme.sica.infrastructure.security;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHasher {

    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
    }

    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (hashedPassword == null || hashedPassword.trim().isEmpty()) {
            return false;
        }
        // 1. Verificación BCrypt (soporta $2a$, $2b$, $2y$, $2x$)
        if (hashedPassword.startsWith("$2a$") || hashedPassword.startsWith("$2b$")
                || hashedPassword.startsWith("$2y$") || hashedPassword.startsWith("$2x$")) {
            try {
                return BCrypt.checkpw(plainPassword, hashedPassword);
            } catch (Exception e) {
                System.err.println("[PasswordHasher Warning] Error comparando BCrypt: " + e.getMessage());
            }
        }
        // 2. Fallback de comparación directa por si se insertó texto plano
        return plainPassword.equals(hashedPassword);
    }
}
