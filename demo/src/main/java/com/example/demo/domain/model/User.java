package com.example.demo.domain.model;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_tipo_documento", nullable = false)
    private Integer idTipoDocumento;

    @Column(unique = true, nullable = false)
    private String nuip;


    @Column(nullable = false)
    private String contraseña;


public void setContraseña(String contraseña) {
    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    this.contraseña = passwordEncoder.encode(contraseña);
}

}
