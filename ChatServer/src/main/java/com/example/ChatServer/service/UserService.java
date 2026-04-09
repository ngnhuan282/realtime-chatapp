package com.example.ChatServer.service;

import com.example.ChatServer.entity.User;
import com.example.ChatServer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Lấy tất cả trừ mình (dùng cho danh sách gợi ý)
    public List<User> getAllUsersExceptMe(Integer excludeId) {
        return userRepository.findAll().stream()
                .filter(user -> !user.getId().equals(excludeId))
                .collect(Collectors.toList());
    }

    // Tìm theo SĐT (mới)
    public User findByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với SĐT: " + phoneNumber));
    }
}