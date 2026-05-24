package com.example.Piroin.project.domain.assignment.controller;

import com.example.Piroin.project.domain.assignment.dto.*;
import com.example.Piroin.project.domain.assignment.entity.DeleteAssignmentResponse;
import com.example.Piroin.project.domain.assignment.service.AssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/assignments")
@Tag(name = "피로체크 과제 관리", description = "피로체크 과제 관리 API")
public class AssignmentController {

    private final AssignmentService assignmentService;

    // 1. 과제 생성
    @Operation(summary = "과제 생성", description = "새로운 과제를 운영진이 입력하여 생성합니다.")
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateAssignmentResponse createAssignment(
            @RequestBody CreateAssignmentRequest request
    ) {
        return assignmentService.createAssignment(request);
    }

    // 2. 과제 수정
    @Operation(summary = "과제 수정", description = "과제에 대한 과제명/주차/날짜를 운영진이 수정합니다.")
    @PatchMapping("/modify/{assignmentId}")
    public ModifyAssignmentResponse modifyAssignment(
            @PathVariable Integer assignmentId,
            @RequestBody ModifyAssignmentRequest request
    ) {
        return assignmentService.modifyAssignment(assignmentId, request);
    }


    // 3. 과제 삭제
    @Operation(summary = "과제 삭제", description = "운영진이 과제를 삭제합니다.")
    @DeleteMapping("/{assignmentId}")
    @ResponseStatus(HttpStatus.OK)
    public DeleteAssignmentResponse deleteAssignment(
            @PathVariable Integer assignmentId
    ) {

        return assignmentService.deleteAssignment(assignmentId);
    }

    // 4. 나의 과제 상태 조회 (부원)
    @Operation(summary = "나의 과제 조회", description = "부원이 본인의 과제 상태를 조회합니다.")
    @GetMapping("/me/{week}")
    public GetMyAssignmentsResponse getMyAssignments(
            @PathVariable String week,
            Authentication authentication
    ) {

        Long userId = Long.valueOf(authentication.getName());

        return assignmentService.getMyAssignments(userId, week);
    }


}

