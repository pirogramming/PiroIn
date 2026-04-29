package com.example.Piroin.project.domain.curriculum.dto;

public class CurriculumResDTO {
    private Long id;
    private String title;

    public CurriculumResDTO(Long id, String title) {
        this.id = id;
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
}
