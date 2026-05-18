package com.example.Piroin.project.domain.user.service;

import com.example.Piroin.project.domain.user.dto.StudentListResponse;
import com.example.Piroin.project.domain.user.entity.User;
import com.example.Piroin.project.domain.user.enums.Role;
import com.example.Piroin.project.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;

    // 전체 부원 목록 조회
    public List<StudentListResponse> getStudentList() {

        List<User> students = userRepository.findByRole(Role.MEMBER);

        return students.stream()
                .map(user -> new StudentListResponse(
                        user.getId(),
                        user.getName()
                ))
                .toList();
    }

    // 부원 이름 검색
    public List<StudentListResponse> searchStudents(String name) {

        List<User> students =
                userRepository.findByRoleAndNameContaining(Role.MEMBER, name);

        return students.stream()
                .map(user -> new StudentListResponse(
                        user.getId(),
                        user.getName()
                ))
                .toList();
    }
}