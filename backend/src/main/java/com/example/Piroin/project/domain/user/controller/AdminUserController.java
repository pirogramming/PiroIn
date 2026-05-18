package com.example.Piroin.project.domain.user.controller;

import com.example.Piroin.project.domain.assignment.service.AssignmentService;
import com.example.Piroin.project.domain.attendance.dto.ApiResponse;
import com.example.Piroin.project.domain.user.dto.StudentListResponse;
import com.example.Piroin.project.domain.user.dto.StudentListResponse;
import com.example.Piroin.project.domain.user.dto.StudentStatusResponse;
import com.example.Piroin.project.domain.user.dto.StudentWeeklyStatusResponse;
import com.example.Piroin.project.domain.user.service.AdminUserService;
import com.example.Piroin.project.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "관리자용 user 정보", description = "관지라용 user 관련 API")
@RequestMapping("/api/admin")
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final AssignmentService assignmentService;

    // 전체 부원 목록 조회
    @Operation(summary = "전체 부원 이름 목록 조회", description = "운영진이 전체 부원의 이름 목록을 조회합니다.")
    @GetMapping("/studentlist")
    public List<StudentListResponse> getStudentList() {
        return adminUserService.getStudentList();
    }


    @Operation(summary = "부원 이름 검색", description = "운영진이 부원의 이름을 검색합니다(포함된 글자로 검색도 가능)")
    @GetMapping("/studentlist/search")
    public List<StudentListResponse> searchStudents(
            @RequestParam String name
    ) {
        return adminUserService.searchStudents(name);
    }

    // 특정 부원의 과제/출석 정보 조회
    @Operation(
            summary = "특정 학생 주간 과제/출석 조회",
            description = "운영진이 특정 학생의 주차별 과제 및 출석 상태를 조회합니다."
    )
    @GetMapping("/admin/student/{userId}/status/{week}")
    public ApiResponse<StudentWeeklyStatusResponse> getStudentWeeklyStatus(
            @PathVariable Long userId,
            @PathVariable Long week
    ) {

        return ApiResponse.success(
                assignmentService.getStudentWeeklyStatus(
                        userId,
                        week
                )
        );
    }



}