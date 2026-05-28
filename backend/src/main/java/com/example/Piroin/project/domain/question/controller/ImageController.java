package com.example.Piroin.project.domain.question.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/* 이미지 업로드/조회 컨트롤러

[흐름]
1. POST /api/images 로 이미지 파일 전송 → URL 반환
2. 반환된 URL을 질문/댓글 등록 요청의 imageUrl 필드에 포함
3. GET /api/images/{filename} 으로 저장된 이미지 조회 */
@RestController
@RequestMapping("/api/images")
public class ImageController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    /* 이미지 업로드
    POST /api/images
    Content-Type: multipart/form-data */
    @PostMapping
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        // 저장 폴더 없으면 생성
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 파일명 중복 방지: UUID + 원본 확장자
        String originalName = file.getOriginalFilename();
        String extension = originalName.substring(originalName.lastIndexOf("."));
        String savedName = UUID.randomUUID() + extension;

        // 파일 저장
        file.transferTo(new File(uploadDir + savedName));

        return ResponseEntity.ok(Map.of("imageUrl", "/api/images/" + savedName));
    }

    // 이미지 조회
    // GET /api/images/{filename}
    @GetMapping("/{filename}")
    public ResponseEntity<byte[]> getImage(@PathVariable String filename) throws IOException {
        File file = new File(uploadDir + filename);

        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        String contentType = filename.endsWith(".png") ? "image/png" : "image/jpeg";

        return ResponseEntity.ok()
                .header("Content-Type", contentType)
                .body(java.nio.file.Files.readAllBytes(file.toPath()));
    }
}