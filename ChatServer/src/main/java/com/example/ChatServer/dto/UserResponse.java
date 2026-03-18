package com.example.ChatServer.dto;

import lombok.Data;

@Data
public class UserResponse {
    private Integer id;
    private String username;
    private String displayName;
    private String avatar;
}
