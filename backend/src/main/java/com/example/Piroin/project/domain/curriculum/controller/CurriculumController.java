package com.example.Piroin.project.domain.curriculum.controller;

import com.example.Piroin.project.domain.curriculum.dto.CurriculumResDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/curriculums")
public class CurriculumController {

    @GetMapping
    public List<CurriculumResDTO> getCurriculums() {
        return List.of(
                new CurriculumResDTO(1L, "1주차 - OT"),
                new CurriculumResDTO(2L, "2주차 - Java 기초")
        );
    }
}
