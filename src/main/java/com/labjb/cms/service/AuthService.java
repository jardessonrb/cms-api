package com.labjb.cms.service;

import com.labjb.cms.domain.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthService {

    public User obtemUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Usuário não autenticado");
        }
        
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof User) {
            return (User) principal;
        }
        
        throw new RuntimeException("Tipo de usuário não suportado: " + principal.getClass().getName());
    }
}
