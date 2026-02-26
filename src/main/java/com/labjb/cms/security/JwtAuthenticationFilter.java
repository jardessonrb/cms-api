package com.labjb.cms.security;

import com.labjb.cms.service.JwtService;
import com.labjb.cms.shared.errors.exception.JwtTokenInvalidException;
import com.labjb.cms.shared.errors.exception.JwtTokenMissingException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            if (requiresAuthentication(request)) {
                handleError(response, 401, "Token JWT não encontrado no cabeçalho Authorization");
                throw new JwtTokenMissingException("Token JWT não encontrado no cabeçalho Authorization");
            }
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        
        try {
            userEmail = jwtService.extractUsername(jwt);
        } catch (Exception e) {
            handleError(response, 401, "Token JWT inválido ou Expirado");
            throw new JwtTokenInvalidException("Token JWT inválido: " + e.getMessage());
        }

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails;
            
            try {
                userDetails = this.userDetailsService.loadUserByUsername(userEmail);
            } catch (Exception e) {
                handleError(response, 403, "Usuário não encontrado ou sem permissão.");
                throw new JwtTokenInvalidException("Usuário não encontrado: " + e.getMessage());
            }

            try {
                if (jwtService.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    handleError(response, 401, "Token JWT inválido ou expirado");
                    throw new JwtTokenInvalidException("Token JWT inválido ou expirado");
                }
            } catch (Exception e) {
                handleError(response, 403, "Erro na validação do token");
                throw new JwtTokenInvalidException("Erro na validação do token: " + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
    
    private boolean requiresAuthentication(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return false;
        }

        String path = request.getRequestURI();
        return !path.equals("/auth/login") &&
               !path.equals("/auth/register") &&
               !path.equals("/ranking/visualizacao") &&
               !path.startsWith("/v3/api-docs") &&
               !path.startsWith("/swagger-ui") && 
               !path.equals("/swagger-ui.html") &&
               !path.equals("/actuator/health");
    }
    private void handleError(HttpServletResponse response, int status, String mensagem) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"mensagem\": \"" + mensagem + "\"}");
    }
}
