package com.example.ChatServer.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class GroupResponse {
    private Integer id;
    private String groupName;
    private UserResponse creator;
    private List<UserResponse> members;
    private LocalDateTime createdAt;
}
