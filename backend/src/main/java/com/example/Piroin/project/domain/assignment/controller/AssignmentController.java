package com.example.Piroin.project.domain.assignment.controller;

import com.example.Piroin.project.domain.assignment.dto.CreateAssignmentRequest;
import com.example.Piroin.project.domain.assignment.dto.CreateAssignmentResponse;
import com.example.Piroin.project.domain.assignment.service.AssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateAssignmentResponse createAssignment(
            @RequestBody CreateAssignmentRequest request
    ) {
        return assignmentService.createAssignment(request);
    }
}

