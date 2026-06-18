package com.example.Piroin.project.domain.question.entity;

import com.example.Piroin.project.domain.curriculum.entity.StudySession;
import com.example.Piroin.project.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "question")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private StudySession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(columnDefinition = "TEXT")
    private String content;

    /*
    이미지 URL 목록을 JSON 배열 문자열로 저장
    예시: ["\/api\/images\/uuid1.png","\/api\/images\/uuid2.jpg"]
    기존 단일 URL(하위 호환): 기존 데이터에 imageUrl이 JSON 배열이 아닌 단일 URL 문자열로 저장된 경우
    getImageUrls()에서 정상적으로 파싱하여 1개짜리 리스트로 반환
    */
    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "is_resolved", nullable = false)
    private Boolean isResolved;

    @Column(name = "like_count", nullable = false)
    private Integer likeCount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // 이미지 URL 목록 조회 (JSON 배열 → List<String> 변환)
    @Transient
    public List<String> getImageUrls() {
        return parseImageUrls(this.imageUrl);
    }

    // 이미지 URL 목록 저장 (List<String> → JSON 배열 문자열 변환)
    public void setImageUrls(List<String> imageUrls) {
        this.imageUrl = serializeImageUrls(imageUrls);
        this.updatedAt = LocalDateTime.now();
    }

    // 댓글이 새로 달리면 미해결로 되돌리도록
    public void markUnresolved() {
        this.isResolved = false;
        this.updatedAt = LocalDateTime.now();
    }

    // 좋아요 추가 시 호출
    public void increaseLikeCount() {
        this.likeCount++;
        this.updatedAt = LocalDateTime.now();
    }

    // 좋아요 취소 시 호출 (0 아래로 내려가지 않도록 방어)
    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
        this.updatedAt = LocalDateTime.now();
    }

    // 질문 내용 수정
    public void updateContent(String content) {
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    // 질문 소프트 삭제 (DB에서 실제로 지우지 않고 deleted_at에 시각 기록)
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // 질문 상태를 해결 완료로 변경 (관리자만 호출)
    public void markResolved() {
        this.isResolved = true;
        this.updatedAt = LocalDateTime.now();
    }

    // JSON 배열 문자열 파싱 유틸 (하위 호환: 기존 단일 URL도 1개짜리 리스트로 반환)
    public static List<String> parseImageUrls(String raw) {
        if (raw == null || raw.isBlank()) {
            return new ArrayList<>();
        }
        String trimmed = raw.trim();
        // JSON 배열 형태인 경우
        if (trimmed.startsWith("[")) {
            // 간단한 JSON 배열 파싱 (외부 라이브러리 없이)
            String inner = trimmed.substring(1, trimmed.length() - 1).trim();
            if (inner.isEmpty()) return new ArrayList<>();
            return Arrays.stream(inner.split(","))
                    .map(s -> s.trim().replaceAll("^\"|\"$", ""))
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }
        // 기존 단일 URL (하위 호환)
        List<String> list = new ArrayList<>();
        list.add(trimmed);
        return list;
    }

    // List<String> → JSON 배열 문자열 직렬화 유틸
    public static String serializeImageUrls(List<String> urls) {
        if (urls == null || urls.isEmpty()) {
            return null;
        }
        String joined = urls.stream()
                .map(url -> "\"" + url.replace("\"", "\\\"") + "\"")
                .collect(Collectors.joining(","));
        return "[" + joined + "]";
    }
}