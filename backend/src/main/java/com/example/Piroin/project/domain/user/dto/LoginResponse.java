package com.example.Piroin.project.domain.user.dto;

import com.example.Piroin.project.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class LoginResponse {

    @Schema(description = "유저 고유 ID", example = "1")
    private Long id;

    @Schema(description = "유저 이름", example = "김피로")
    private String name;

    @Schema(description = "유저 권한", example = "MEMBER")
    private String role;

    @Schema(description = "JWT 액세스 토큰")
    private String token;

    public LoginResponse(User user, String token) {
        this.id = user.getId();
        this.name = user.getName();
        this.role = user.getRole().name();
        this.token = token;
    }
}
