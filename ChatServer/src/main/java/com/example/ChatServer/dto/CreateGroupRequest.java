package com.example.ChatServer.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateGroupRequest {
    private String groupName;
    private Integer creatorId;
    private List<Integer> memberIds;
}
