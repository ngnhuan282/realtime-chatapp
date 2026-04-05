package com.example.ChatServer.dto;

import lombok.Data;

@Data
public class MessageDTO {
    private Integer senderId;
    private Integer receiverId;
    private Integer groupId;
    private String content;
    private long timestamp;
    private String messageType;
}