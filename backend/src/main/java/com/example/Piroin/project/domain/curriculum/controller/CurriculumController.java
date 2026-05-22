package com.example.Piroin.project.domain.curriculum.controller;

import com.example.Piroin.project.domain.curriculum.dto.CurriculumReqDTO;
import com.example.Piroin.project.domain.curriculum.dto.CurriculumResDTO;
import com.example.Piroin.project.domain.curriculum.service.CurriculumService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/curriculums")
@RequiredArgsConstructor
public class CurriculumController {

    private final CurriculumService curriculumService;

    @PostMapping
    public ResponseEntity<CurriculumResDTO.CreateDayRes> createDay(
            @RequestBody CurriculumReqDTO.CreateDayReq req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(curriculumService.createDay(req));
    }

    @PatchMapping("/{sessionId}")
    public ResponseEntity<CurriculumResDTO.UpdateSessionRes> updateSession(
            @PathVariable Long sessionId,
            @RequestBody CurriculumReqDTO.UpdateSessionReq req) {
        CurriculumResDTO.UpdateSessionRes response = curriculumService.updateSession(sessionId, req);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Map<String, String>> deleteSession(@PathVariable Long sessionId) {
        curriculumService.deleteSession(sessionId);
        return ResponseEntity.ok(Map.of("message", "세션이 정상적으로 삭제되었습니다."));
    }
}
