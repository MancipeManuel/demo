package com.example.demo.domain.model;

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

    @ManyToOne
    @JoinColumn(name = "id_tipo_documento", nullable = false)
    private DocumentType documentType;  // Ahora el nombre es coherente


    @Column(unique = true, nullable = false)
    private String nuip;

    @Column(unique = true, nullable = false)
    private String password;
    
    private String email;

    private String resetToken; 

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Transient
    private Long roleId;

    @Transient
    private Long documentTypeId;
}
