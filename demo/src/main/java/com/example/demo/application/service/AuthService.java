package com.example.demo.application.service;

import com.example.demo.domain.model.DocumentType;
import com.example.demo.domain.model.Role;
import com.example.demo.domain.model.User;
import com.example.demo.infrastructure.repository.DocumentTypeRepository;
import com.example.demo.infrastructure.repository.RoleRepository;
import com.example.demo.infrastructure.repository.UserRepository;
import com.example.demo.infrastructure.security.JwtUtil;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final RoleRepository roleRepository;
    private final DocumentTypeRepository documentTypeRepository;

    /**
     * Autenticar usuario con NUIP y contraseña.
     */
    public Optional<String> authenticate(String nuip, String password) {
        return userRepository.findByNuip(nuip)
                .filter(user -> passwordEncoder.matches(password, user.getPassword()))
                .map(user -> jwtUtil.generateToken(user.getNuip(), user.getRole().getName()));
    }

    /**
     * Registrar un nuevo usuario con un rol por defecto.
     */
    public boolean registerUser(User user) {
        if (userRepository.existsByNuip(user.getNuip())) {
            throw new IllegalArgumentException("El NUIP ya está registrado");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("El correo ya está en uso");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede ser nula o vacía");
        }
        
        // ✅ Validar y asignar DocumentType
        DocumentType documentType = documentTypeRepository.findById(user.getDocumentTypeId())
            .orElseThrow(() -> new IllegalArgumentException("Tipo de documento no válido"));
        user.setDocumentType(documentType);
    
        // ✅ Validar y asignar Role
        if (user.getRoleId() == null) {
            // 🔥 Asignar un rol por defecto si no se envía
            Role defaultRole = roleRepository.findByName("CANDIDATO") // Busca el rol "USER" por defecto
                .orElseThrow(() -> new IllegalArgumentException("Rol por defecto no encontrado"));
            user.setRole(defaultRole);
        } else {
            Role role = roleRepository.findById(user.getRoleId())
                .orElseThrow(() -> new IllegalArgumentException("Rol no válido"));
            user.setRole(role);
        }

        
        // ✅ Encriptar contraseña
        user.setPassword(passwordEncoder.encode(user.getPassword()));
    
        userRepository.save(user);
        return true;
    }
    
            

    /**
     * Actualizar contraseña del usuario si la contraseña actual es válida.
     */
    public boolean updatePassword(String nuip, String oldPassword, String newPassword) {
        return userRepository.findByNuip(nuip)
                .filter(user -> passwordEncoder.matches(oldPassword, user.getPassword()))
                .map(user -> {
                    user.setPassword(passwordEncoder.encode(newPassword));
                    userRepository.save(user);
                    return true;
                })
                .orElse(false);
    }

    /**
     * Generar un token para recuperación de contraseña y enviarlo por correo.
     */
    public boolean generateResetToken(String email) {
        return userRepository.findByEmail(email)
                .map(user -> {
                    String token = UUID.randomUUID().toString();
                    user.setResetToken(token);
                    userRepository.save(user);
                    sendResetEmail(user.getEmail(), token);
                    return true;
                })
                .orElse(false);
    }

    /**
     * Enviar un correo con el token de recuperación.
     */
    private void sendResetEmail(String email, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(email);
            helper.setSubject("Recuperación de Contraseña");
            helper.setText("Para restablecer tu contraseña, usa el siguiente token: " + token, true);
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Restablecer contraseña con un token válido.
     */
    public boolean resetPassword(String token, String newPassword) {
        return userRepository.findByResetToken(token)
                .map(user -> {
                    user.setPassword(passwordEncoder.encode(newPassword));
                    user.setResetToken(null);
                    userRepository.save(user);
                    return true;
                })
                .orElse(false);
    }

    /**
     * Cargar un usuario por su NUIP, usado en autenticación de Spring Security.
     */
    public UserDetails loadUserByUsername(String nuip) throws UsernameNotFoundException {
        return userRepository.findByNuip(nuip)
                .map(user -> org.springframework.security.core.userdetails.User.builder()
                        .username(user.getNuip())
                        .password(user.getPassword())
                        .roles(user.getRole().getName()) // Convertir rol en formato adecuado
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con NUIP: " + nuip));
    }
}
