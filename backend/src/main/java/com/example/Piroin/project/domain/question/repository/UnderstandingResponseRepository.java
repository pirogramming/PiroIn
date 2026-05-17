package com.example.Piroin.project.domain.question.repository;

import com.example.Piroin.project.domain.question.entity.UnderstandingCheck;
import com.example.Piroin.project.domain.question.entity.UnderstandingResponse;
import com.example.Piroin.project.domain.question.enums.UnderstandResChoice;
import com.example.Piroin.project.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UnderstandingResponseRepository extends JpaRepository<UnderstandingResponse, Long> {
    Optional<UnderstandingResponse> findByCheckAndUser(UnderstandingCheck check, User user);

    /*
    유저가 특정 이해도 체크에 이미 응답했는지 여부
    용도: 중복 응답 방지
    */
    boolean existsByCheckAndUser(UnderstandingCheck check, User user);

    /*
    특정 이해도 체크에서 특정 선택지(O 또는 X)의 응답 수
    용도: 운영진에게 O 몇 명 / X 몇 명 표시 시
    */
    int countByCheckAndChoice(UnderstandingCheck check, UnderstandResChoice choice);
}
