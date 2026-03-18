package com.example.ChatServer.service;

import com.example.ChatServer.dto.request.LoginRequest;
import com.example.ChatServer.dto.response.LoginResponse;
import com.example.ChatServer.entity.User;
import com.example.ChatServer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    public LoginResponse login(LoginRequest request) {
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getPassword().equals(request.getPassword())) {
                return new LoginResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getDisplayName(),
                        "Đăng nhập thành công!"
                );
            }
        }
        return null;
    }
}