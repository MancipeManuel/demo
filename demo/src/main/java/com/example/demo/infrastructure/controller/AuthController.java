package com.example.demo.infrastructure.controller;

import com.example.demo.application.service.AuthService;
import com.example.demo.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ✅ Login de usuario
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> credentials) {
        String nuip = credentials.get("nuip");
        String password = credentials.get("password");

        Optional<String> token = authService.authenticate(nuip, password);

        return token.map(jwt -> ResponseEntity.ok(Map.of("token", jwt)))
                    .orElseGet(() -> ResponseEntity.status(401).body(Map.of("error", "Credenciales incorrectas")));
    }

    // ✅ Registro de usuario
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        boolean userCreated = authService.registerUser(user);
    
        if (userCreated) {
            return ResponseEntity.status(201).body("Usuario registrado exitosamente");
        } else {
            return ResponseEntity.status(400).body("El usuario ya existe");
        }
    }
}
