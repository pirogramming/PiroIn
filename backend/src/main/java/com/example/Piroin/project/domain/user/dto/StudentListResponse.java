package com.example.Piroin.project.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StudentListResponse {
    private Long userId;

    private String name;
}
