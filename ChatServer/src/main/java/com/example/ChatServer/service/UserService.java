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

    /**
     * Lấy danh sách tất cả người dùng trừ người đang đăng nhập
     * @param excludeId ID của người dùng hiện tại
     * @return Danh sách User
     */
    public List<User> getAllUsersExceptMe(Integer excludeId) {
        return userRepository.findAll().stream()
                .filter(user -> !user.getId().equals(excludeId))
                .collect(Collectors.toList());
    }
}