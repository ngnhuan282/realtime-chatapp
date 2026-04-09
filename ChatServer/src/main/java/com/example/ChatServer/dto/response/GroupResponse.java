package com.example.ChatServer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class GroupResponse {

    private Integer id;
    private String groupName;
    private UserResponse creator;
    private List<UserResponse> members;
    private LocalDateTime createdAt;

    // Constructor thủ công
    public GroupResponse(Integer id, String groupName, UserResponse creator,
                         List<UserResponse> members, LocalDateTime createdAt) {
        this.id = id;
        this.groupName = groupName;
        this.creator = creator;
        this.members = members;
        this.createdAt = createdAt;
    }
}