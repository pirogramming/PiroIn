package com.example.Piroin.project.domain.user.service;

import com.example.Piroin.project.domain.user.entity.User;
import com.example.Piroin.project.domain.user.exception.InvalidLoginException;
import com.example.Piroin.project.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User login(String name, String password) {
        User user = userRepository.findByName(name)
                .orElseThrow(() -> new InvalidLoginException("해당 사용자가 존재하지 않습니다."));

        if (!user.getPassword().equals(password)) {
            throw new InvalidLoginException("비밀번호가 일치하지 않습니다.");
        }

        return user;
    }
}
