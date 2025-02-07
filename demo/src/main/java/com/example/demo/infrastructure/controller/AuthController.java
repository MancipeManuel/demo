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

@PostMapping("/register")
public ResponseEntity<String> register(@RequestBody User user) {
    try {
        boolean userCreated = authService.registerUser(user);
        if (userCreated) {
            return ResponseEntity.status(201).body("Usuario registrado exitosamente");
        } else {
            return ResponseEntity.status(400).body("El usuario ya existe");
        }
    } catch (IllegalArgumentException e) {
        return ResponseEntity.status(400).body(e.getMessage());
    }
}

    @PutMapping("/update-password")
    public ResponseEntity<String> updatePassword(@RequestBody Map<String, String> request) {
        String nuip = request.get("nuip");
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");

        boolean passwordUpdated = authService.updatePassword(nuip, oldPassword, newPassword);

        if (passwordUpdated) {
            return ResponseEntity.ok("Contraseña actualizada exitosamente");
        } else {
            return ResponseEntity.status(400).body("Error al actualizar la contraseña, verifica tu contraseña actual");
        }
    }
    @PostMapping("/request-password-reset")
    public ResponseEntity<String> requestPasswordReset(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        if (authService.generateResetToken(email)) {
            return ResponseEntity.ok("Se ha enviado un correo con el token de recuperación.");
        } else {
            return ResponseEntity.status(400).body("Correo no encontrado.");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");

        if (authService.resetPassword(token, newPassword)) {
            return ResponseEntity.ok("Contraseña restablecida con éxito.");
        } else {
            return ResponseEntity.status(400).body("Token inválido o expirado.");
        }
    }
}
