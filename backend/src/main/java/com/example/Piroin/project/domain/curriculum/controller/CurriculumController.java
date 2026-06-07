package com.example.Piroin.project.domain.curriculum.controller;

import com.example.Piroin.project.domain.curriculum.dto.CurriculumReqDTO;
import com.example.Piroin.project.domain.curriculum.dto.CurriculumResDTO;
import com.example.Piroin.project.domain.curriculum.service.CurriculumService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/curriculums")
@RequiredArgsConstructor
public class CurriculumController {

    private final CurriculumService curriculumService;

    @GetMapping
    public ResponseEntity<List<CurriculumResDTO.CreateDayRes>> getAllDays() {
        return ResponseEntity.ok(curriculumService.getAllDays());
    }

    @PostMapping
    public ResponseEntity<CurriculumResDTO.CreateDayRes> createDay(
            @RequestBody @Valid CurriculumReqDTO.CreateDayReq req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(curriculumService.createDay(req));
    }

    @PatchMapping("/{sessionDate}")
    public ResponseEntity<CurriculumResDTO.CreateDayRes> updateDay(
            @PathVariable LocalDate sessionDate,
            @RequestBody @Valid CurriculumReqDTO.UpdateDayReq req) {
        return ResponseEntity.ok(curriculumService.updateDay(sessionDate, req));
    }

    @DeleteMapping("/{sessionDate}")
    public ResponseEntity<Map<String, String>> deleteDay(@PathVariable LocalDate sessionDate) {
        curriculumService.deleteDay(sessionDate);
        return ResponseEntity.ok(Map.of("message", "세션이 정상적으로 삭제되었습니다."));
    }
}
