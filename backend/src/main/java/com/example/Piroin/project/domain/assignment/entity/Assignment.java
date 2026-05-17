package com.example.Piroin.project.domain.assignment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "assignment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // SERIAL 타입에 매칭

    @Column(nullable = false)
    private String title;

    @Column(length = 255)
    private String week;

    @Column(name = "session_date")
    private LocalDate sessionDate; // DATE 타입에 매칭

}