package com.example.demo.application.service;

import com.example.demo.domain.model.User;
import com.example.demo.infrastructure.repository.UserRepository;
import com.example.demo.infrastructure.security.JwtUtil;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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

    public Optional<String> authenticate(String nuip, String password) {
        return userRepository.findByNuip(nuip)
                .filter(user -> passwordEncoder.matches(password, user.getPassword()))
                .map(user -> {
                    String token = jwtUtil.generateToken(user.getNuip());
                    System.out.println("Generated JWT: " + token);
                    return token;
                });
    }

    public boolean registerUser(User user) {
        if (userRepository.existsByNuip(user.getNuip())) {
            return false; // El NUIP ya está registrado
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("El correo ya está en uso");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede ser nula o vacía");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword())); // Encriptar contraseña
        userRepository.save(user);
        return true; 
    }

      public boolean updatePassword(String nuip, String oldPassword, String newPassword) {
        Optional<User> optionalUser = userRepository.findByNuip(nuip);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                return false; 
            }
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            return true;
        }
        return false; 
    }
    public boolean generateResetToken(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            String token = UUID.randomUUID().toString();
            
            user.setResetToken(token);
            userRepository.save(user);

            sendResetEmail(user.getEmail(), token);
            return true;
        } else {
            System.out.println("No se encontró usuario con el email: " + email);
        }

        return false;
    }

    private void sendResetEmail(String email, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(email);
            helper.setSubject("Recuperación de Contraseña");
            helper.setText("Para restablecer tu contraseña, usa el siguiente token q bonitos aretes: " + token, true);
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean resetPassword(String token, String newPassword) {
        Optional<User> optionalUser = userRepository.findByResetToken(token);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setResetToken(null);
            userRepository.save(user);
            return true;
        }
        return false;
    }
   
}
