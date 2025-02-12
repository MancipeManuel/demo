package com.example.demo.infrastructure.controller;

import com.example.demo.application.service.AuthService;
import com.example.demo.domain.model.User;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ✅ Login de usuario
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> credentials) {
        return authService.authenticate(credentials.get("nuip"), credentials.get("password"))
                .map(jwt -> ResponseEntity.ok(Map.of("token", jwt)))
                .orElseGet(() -> ResponseEntity.status(401).body(Map.of("error", "Credenciales incorrectas")));
    }

    // ✅ Registro de usuario
@PostMapping("/register")
public ResponseEntity<String> register(@RequestBody User user) {
    try {
        boolean isRegistered = authService.registerUser(user);

        if (isRegistered) {
            return ResponseEntity.status(HttpStatus.CREATED).body("Usuario registrado exitosamente");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El usuario ya existe o los datos son inválidos");
        }
    } catch (RuntimeException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error en el registro: " + e.getMessage());
    }
}


    // ✅ Actualizar contraseña
    @PutMapping("/update-password")
    public ResponseEntity<String> updatePassword(@RequestBody Map<String, String> request) {
        return authService.updatePassword(request.get("nuip"), request.get("oldPassword"), request.get("newPassword"))
                ? ResponseEntity.ok("Contraseña actualizada exitosamente")
                : ResponseEntity.status(400).body("Error al actualizar la contraseña, verifica tu contraseña actual");
    }

    // ✅ Solicitar token de recuperación de contraseña
    @PostMapping("/request-password-reset")
    public ResponseEntity<String> requestPasswordReset(@RequestBody Map<String, String> request) {
        return authService.generateResetToken(request.get("email"))
                ? ResponseEntity.ok("Se ha enviado un correo con el token de recuperación.")
                : ResponseEntity.status(400).body("Correo no encontrado.");
    }

    // ✅ Restablecer contraseña
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> request) {
        return authService.resetPassword(request.get("token"), request.get("newPassword"))
                ? ResponseEntity.ok("Contraseña restablecida con éxito.")
                : ResponseEntity.status(400).body("Token inválido o expirado.");
    }
}
