package com.example.Piroin.project.domain.deposit.controller;

import com.example.Piroin.project.domain.deposit.dto.AdminDepositViewResponse;
import com.example.Piroin.project.domain.deposit.dto.DepositResponse;
import com.example.Piroin.project.domain.deposit.dto.UpdateDefenceRequest;
import com.example.Piroin.project.domain.deposit.dto.UpdateDefenceResponse;
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


    // 1. 나의 보증금 조회
    @Operation(summary = "(부원) 나의 보증금 조회", description = "사용자가 본인의 남은 보증금, 차감된 보증금, 방어권을 조회합니다.")
    @GetMapping("/me")
    public DepositResponse getMyDeposit(
            Authentication authentication
    ) {

        Long userId = Long.valueOf(authentication.getName());

        return depositService.getMyDeposit(userId);
    }


    // 2. 운영진이 특정 부원의 보증금을 조회
    @Operation(summary = "(운영진) 특정 부원 보증금 조회", description = "운영진이 특정 부원의 보증금 상태를 조회합니다.")
    @GetMapping("/{userId}/deposit/view")
    public AdminDepositViewResponse getUserDeposit(
            @PathVariable Long userId
    ) {
        return depositService.getUserDeposit(userId);
    }

    // 3. 운영진이 특정 부원의 보증금 정보를 수정
    @Operation(summary = "(운영진) 특정 부원 보증금 방어권 수정", description = "운영진이 특정 부원의 보증금 방어권 금액을 수정합니다.")
    @PatchMapping("/{userId}/deposit/defence")
    public UpdateDefenceResponse updateUserDefence(
            @PathVariable Long userId,
            @RequestBody UpdateDefenceRequest request
    ) {
        return depositService.updateUserDefence(userId, request);
    }
}