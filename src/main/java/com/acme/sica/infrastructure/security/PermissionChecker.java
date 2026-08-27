package com.acme.sica.infrastructure.security;

import com.acme.sica.application.port.out.UsuarioRepository;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PermissionChecker {

    private final UsuarioRepository usuarioRepository;
    private final Map<Long, Set<String>> permissionCache = new ConcurrentHashMap<>();

    public PermissionChecker(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public boolean hasPermission(Long roleId, String requiredPermission) {
        if (requiredPermission == null || requiredPermission.trim().isEmpty()) {
            return true;
        }

        Set<String> permissions = permissionCache.computeIfAbsent(roleId, usuarioRepository::findPermissionsByRoleId);
        return permissions.contains(requiredPermission);
    }

    public Set<String> getPermissions(Long roleId) {
        return permissionCache.computeIfAbsent(roleId, usuarioRepository::findPermissionsByRoleId);
    }

    public void clearCache() {
        permissionCache.clear();
    }
}
