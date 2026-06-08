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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    // 단일 이미지 업로드
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
        String savedUrl = saveFile(file);
        return ResponseEntity.ok(Map.of("imageUrl", savedUrl));
    }

    // 다중 이미지 업로드 (최대 5장)
    // POST /api/images/multi
    @PostMapping("/multi")
    @Operation(
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                schema = @Schema(type = "object", requiredProperties = {"files"}))
        )
    )
    public ResponseEntity<Map<String, List<String>>> uploadImages(
            @RequestParam("files") List<MultipartFile> files
    ) throws IOException {
        if (files == null || files.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        // 최대 5장 제한 (서버 부하 방지)
        if (files.size() > 5) {
            return ResponseEntity.badRequest().build();
        }

        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                imageUrls.add(saveFile(file));
            }
        }
        return ResponseEntity.ok(Map.of("imageUrls", imageUrls));
    }

    // 이미지 조회
    // GET /api/images/{filename}
    @GetMapping("/{filename}")
    public ResponseEntity<byte[]> getImage(@PathVariable String filename) throws IOException {
        File file = new File(new File(uploadDir).getAbsoluteFile(), filename);

        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        String contentType = filename.endsWith(".png") ? "image/png" : "image/jpeg";

        return ResponseEntity.ok()
                .header("Content-Type", contentType)
                .body(java.nio.file.Files.readAllBytes(file.toPath()));
    }

    // 파일 저장 공통 로직
    private String saveFile(MultipartFile file) throws IOException {
        File dir = new File(uploadDir).getAbsoluteFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String originalName = file.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }
        String savedName = UUID.randomUUID() + extension;

        File targetFile = new File(dir, savedName);
        file.transferTo(targetFile);

        return "/api/images/" + savedName;
    }
}