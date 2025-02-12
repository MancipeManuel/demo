package com.example.demo.infrastructure.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.example.demo.domain.model.User;

import java.security.Key;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;  

    @Value("${jwt.expiration}")
    private long expirationTime;
    

    private Key getSigningKey() {
        byte[] decodedKey = Base64.getDecoder().decode(secretKey); // 🔹 Decodifica correctamente
        System.out.println("🔑 Clave secreta decodificada: " + new String(decodedKey)); // 🔍 Verifica la clave decodificada
        return Keys.hmacShaKeyFor(decodedKey);
    }

    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getNuip())
                .claim("role", user.getRole().getName()) // ⚡ Cambia a "rol" para coincidir con el token generado
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }    

    public boolean validateToken(String token, String nuip) {
        System.out.println("🔎 Validando Token: " + token);
        System.out.println("🆔 Nuip esperado: " + nuip);
        
        String extractedNuip = extractNuip(token);
        System.out.println("🧐 Nuip extraído del token: " + extractedNuip);
    
        boolean isValid = extractedNuip.equals(nuip) && !isTokenExpired(token);
        System.out.println("✅ Token válido: " + isValid);
        
        return isValid;
    }

    public String extractNuip(String token) {
        String nuip = extractClaim(token, Claims::getSubject);
        System.out.println("🛠 Extraído nuip desde el token: " + nuip);
        return nuip;
    }
    

    public List<String> extractRole(String token) {
        Claims claims = extractAllClaims(token);
        Object roleObj = claims.get("role"); // ✅ Extraer "role" en lugar de "rolees"
    
        if (roleObj instanceof String) { 
            return List.of((String) roleObj); // ✅ Convertir en lista de un solo elemento
        }
    
        return Collections.emptyList();
    }
    
    

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private boolean isTokenExpired(String token) {
        boolean expired = extractExpiration(token).before(new Date());
        System.out.println("⏳ ¿Token expirado?: " + expired);
        return expired;
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
