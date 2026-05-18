package com.example.Piroin.project.domain.deposit.controller;

import com.example.Piroin.project.domain.deposit.dto.DepositResponse;
import com.example.Piroin.project.domain.deposit.service.DepositService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/deposit")
@Tag(name = "보증금", description = "보증금 관련 API")
public class DepositController {

    private final DepositService depositService;

    @Operation(summary = "보증금 조회)", description = "사용자가 본인의 남은 보증금, 차감된 보증금, 방어권을 조회합니다.")
    @GetMapping("/me")
    public DepositResponse getMyDeposit(
            Authentication authentication
    ) {

        Long userId = Long.valueOf(authentication.getName());

        return depositService.getMyDeposit(userId);
    }
}