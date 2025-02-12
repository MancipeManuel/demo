package com.example.demo.infrastructure.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    private String extractTokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7); // 🔹 Extrae el token eliminando "Bearer "
        }
        return null;
    }
    
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        
        String token = extractTokenFromRequest(request);
        System.out.println("📢 Token recibido en filtro: " + token);
    
        if (token != null) {
            String extractedNuip = jwtService.extractNuip(token);
            boolean isValid = jwtService.validateToken(token, extractedNuip);
            
            System.out.println("🆔 Nuip extraído del token: " + extractedNuip);
            System.out.println("🔎 ¿Token válido?: " + isValid);
            
            if (isValid) {
                List<String> role = jwtService.extractRole(token);
                System.out.println("🔹 Roles extraídos del token: " + role);
                
                List<SimpleGrantedAuthority> authorities = role.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
    
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(extractedNuip, null, authorities);
    
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                System.out.println("❌ Token inválido o expirado");
            }
        } else {
            System.out.println("⚠️ No se encontró un token en la solicitud");
        }
    
        filterChain.doFilter(request, response);
    }
}
