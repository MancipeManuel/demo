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

    @Column(name = "id_tipo_documento", nullable = false)
    private Integer idDocumentType;

    @Column(unique = true, nullable = false)
    private String nuip;

    @Column(unique = true, nullable = false)
    private String password;

}
