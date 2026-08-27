package com.acme.sica.application.port.out;

public interface PasswordEncoderPort {
    String hashPassword(String plainPassword);
    boolean verifyPassword(String plainPassword, String hashedPassword);
}
