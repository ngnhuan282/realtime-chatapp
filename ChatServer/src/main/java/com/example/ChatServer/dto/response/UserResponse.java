package com.example.ChatServer.dto.response;

import lombok.Data;

@Data
public class UserResponse {
    private Integer id;
    private String username;
    private String displayName;
    private String avatar;
}
