package com.example.ChatServer.controller;

import com.example.ChatServer.entity.User;
import com.example.ChatServer.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public List<User> getAllUsers(@RequestParam Integer excludeId) {
        return userService.getAllUsersExceptMe(excludeId);
    }
}