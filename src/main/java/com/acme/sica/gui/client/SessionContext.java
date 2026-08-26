package com.acme.sica.gui.client;

import com.acme.sica.infrastructure.adapter.in.dto.LoginResponseDTO;

public class SessionContext {

    private static SessionContext instance;

    private String token;
    private Long userId;
    private String username;
    private String nombreCompleto;
    private Long roleId;
    private String roleName;
    private Long empresaId;

    private SessionContext() {}

    public static synchronized SessionContext getInstance() {
        if (instance == null) {
            instance = new SessionContext();
        }
        return instance;
    }

    public void setSession(LoginResponseDTO response) {
        this.token = response.token();
        this.userId = response.userId();
        this.username = response.username();
        this.nombreCompleto = response.nombreCompleto();
        this.roleId = response.roleId();
        this.roleName = response.roleName();
        this.empresaId = response.empresaId();
    }

    public void clear() {
        this.token = null;
        this.userId = null;
        this.username = null;
        this.nombreCompleto = null;
        this.roleId = null;
        this.roleName = null;
        this.empresaId = null;
    }

    public boolean isAuthenticated() {
        return token != null && !token.trim().isEmpty();
    }

    public String getToken() { return token; }
    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getNombreCompleto() { return nombreCompleto; }
    public Long getRoleId() { return roleId; }
    public String getRoleName() { return roleName; }
    public Long getEmpresaId() { return empresaId; }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(roleName);
    }

    public boolean isGuardia() {
        return "GUARDIA".equalsIgnoreCase(roleName) || isAdmin();
    }

    public boolean isFuncionario() {
        return "FUNCIONARIO".equalsIgnoreCase(roleName) || isAdmin();
    }
}
