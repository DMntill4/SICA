package com.acme.sica.infrastructure.security;

import java.util.Set;

public record AuthenticatedUserContext(
    Long userId,
    String username,
    Long roleId,
    String roleName,
    Set<String> permissions,
    String tokenJti
) {}
