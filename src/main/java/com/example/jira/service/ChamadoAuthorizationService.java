package com.example.jira.service;

import org.springframework.stereotype.Service;
import com.example.jira.enums.Escopo;
import com.example.jira.enums.Status;
import com.example.jira.model.Chamado;

@Service
public class ChamadoAuthorizationService {

    public boolean podeVisualizar(Chamado chamado, String role, Long userId) {
        if (isSuperAdmin(role)) {
            return true;
        }

        if (isAdmin(role)) {
            return true;
        }

        if ("ROLE_VISITOR".equals(role)) {
            return chamado.getEscopo() == Escopo.TODOS;
        }

        if (chamado.getEscopo() == Escopo.TODOS) {
            return true;
        }

        return chamado.getTimes().stream()
                .anyMatch(time -> time.getMembros().contains(userId));
    }

    public boolean podeComentar(Chamado chamado, String role, Long userId) {
        if (isSuperAdmin(role) || isAdmin(role)) {
            return chamado.getStatus() != Status.FECHADO;
        }

        if ("ROLE_VISITOR".equals(role) || "ROLE_KYC_ANALYST".equals(role)) {
            return false;
        }

        return chamado.getStatus() != Status.FECHADO && 
               chamado.getTimes().stream()
                      .anyMatch(time -> time.getMembros().contains(userId));
    }

    public boolean podeAlterarStatus(Chamado chamado, String role) {
        if (chamado.getStatus() == Status.FECHADO) {
            return false;
        }

        return isSuperAdmin(role) || isAdmin(role);
    }

    public boolean podeFechar(Chamado chamado, String role) {
        if (chamado.getStatus() == Status.FECHADO) {
            return false;
        }

        return isSuperAdmin(role) || isAdmin(role);
    }

    private boolean isAdmin(String role) {
        return "ROLE_ADMIN".equals(role);
    }

    private boolean isSuperAdmin(String role) {
        return "ROLE_SUPERADMIN".equals(role);
    }
}