package com.example.Piroin.project.domain.user.entity;

import com.example.Piroin.project.domain.user.enums.Role;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 100)
    private String name;

    private String email;
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private Integer generation;
}


