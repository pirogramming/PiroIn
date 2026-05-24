package com.example.Piroin.project.domain.assignment.dto;

import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Getter
public class CreateAssignmentRequest {

    private String title;

    private String week;

    private DayOfWeek day;
}
