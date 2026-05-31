package com.example.Piroin.project.domain.question.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    // consumes 제거
    // Swagger용 어노테이션 추가 (파일 선택 버튼 표시용)
    @PostMapping
    @Operation(
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                schema = @Schema(type = "object", requiredProperties = {"file"}))
        )
    )
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        // 절대 경로로 변환
        File dir = new File(uploadDir).getAbsoluteFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 파일명 중복 방지: UUID + 원본 확장자
        String originalName = file.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }
        String savedName = UUID.randomUUID() + extension;

        // 절대 경로로 파일 저장
        File targetFile = new File(dir, savedName);
        file.transferTo(targetFile);

        return ResponseEntity.ok(Map.of("imageUrl", "/api/images/" + savedName));
    }

    // 이미지 조회
    // GET /api/images/{filename}
    @GetMapping("/{filename}")
    public ResponseEntity<byte[]> getImage(@PathVariable String filename) throws IOException {
        File file = new File(new File(uploadDir).getAbsoluteFile(), filename);  // ← 절대 경로

        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        String contentType = filename.endsWith(".png") ? "image/png" : "image/jpeg";

        return ResponseEntity.ok()
                .header("Content-Type", contentType)
                .body(java.nio.file.Files.readAllBytes(file.toPath()));
    }
}