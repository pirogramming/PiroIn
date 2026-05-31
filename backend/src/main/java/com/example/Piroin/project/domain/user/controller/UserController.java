package com.example.Piroin.project.domain.user.controller;

import com.example.Piroin.project.domain.user.dto.LoginRequest;
import com.example.Piroin.project.domain.user.dto.LoginResponse;
import com.example.Piroin.project.domain.user.entity.User;
import com.example.Piroin.project.domain.user.service.UserService;
import com.example.Piroin.project.global.jwt.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "유저 인증", description = "로그인 / 로그아웃 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Operation(summary = "로그인", description = "이름과 비밀번호로 로그인하고 JWT 토큰을 발급합니다.")
    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        User user = userService.login(request.getName(), request.getPassword());
        String token = jwtUtil.generateToken(user.getId(), user.getRole().name());
        return ResponseEntity.ok(new LoginResponse(user, token));
    }


}