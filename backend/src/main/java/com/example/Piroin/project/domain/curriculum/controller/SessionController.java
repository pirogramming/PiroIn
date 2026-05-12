package com.example.Piroin.project.domain.curriculum.controller;

import com.example.Piroin.project.domain.curriculum.dto.CurriculumResDTO;
import com.example.Piroin.project.domain.curriculum.exception.code.CurriculumSuccessCode;
import com.example.Piroin.project.domain.curriculum.service.CurriculumService;
import com.example.Piroin.project.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sessions")
public class SessionController {
    private final CurriculumService curriculumService;

    @GetMapping
    public ApiResponse<CurriculumResDTO.QnaSessionsResponse> getQnaSessions() {
        CurriculumResDTO.QnaSessionsResponse response = curriculumService.getQnaSessions();

        return ApiResponse.onSuccess(
                CurriculumSuccessCode.QNA_SESSION_LIST_OK,
                response
        );
    }
}
