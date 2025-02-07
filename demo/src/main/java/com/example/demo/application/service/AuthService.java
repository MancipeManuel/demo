package com.example.demo.application.service;

import com.example.demo.domain.model.User;
import com.example.demo.infrastructure.repository.UserRepository;
import com.example.demo.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    // Método para autenticar usuarios
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
            return false; 
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede ser nula o vacía");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        userRepository.save(user);
        return true;
    } 
      public boolean updatePassword(String nuip, String oldPassword, String newPassword) {
        Optional<User> optionalUser = userRepository.findByNuip(nuip);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                return false; // ❌ Contraseña actual incorrecta
            }

            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            return true;
        }

        return false; // ❌ Usuario no encontrado
    }
   
}
