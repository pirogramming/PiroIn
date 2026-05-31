package com.example.Piroin.project.domain.assignment.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@JsonPropertyOrder({"week", "assignments"})
public class GetMyAssignmentsResponse {

    private String week;

    private List<AssignmentInfoResponse> assignments;
}