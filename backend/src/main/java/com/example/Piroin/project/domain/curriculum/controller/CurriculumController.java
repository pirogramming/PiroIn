package com.example.Piroin.project.domain.curriculum.controller;

import com.example.Piroin.project.domain.curriculum.dto.CurriculumReqDTO;
import com.example.Piroin.project.domain.curriculum.dto.CurriculumResDTO;
import com.example.Piroin.project.domain.curriculum.service.CurriculumService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/curriculums")
@RequiredArgsConstructor
public class CurriculumController {

    private final CurriculumService curriculumService;

    @PostMapping
    public ResponseEntity<CurriculumResDTO.CreateSessionRes> createSession(
            @RequestBody CurriculumReqDTO.CreateSessionReq req) {
        CurriculumResDTO.CreateSessionRes response = curriculumService.createSession(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
