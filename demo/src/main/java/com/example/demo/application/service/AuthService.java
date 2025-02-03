package com.example.demo.application.service;

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

    public Optional<String> authenticate(String nuip, String password) {
        return userRepository.findByNuip(nuip)
                .filter(user -> passwordEncoder.matches(password, user.getPassword()))
                .map(user -> {
                    String token = jwtUtil.generateToken(user.getNuip());
                    System.out.println("Generated JWT: " + token); 
                    return token;
                });
    }
}
