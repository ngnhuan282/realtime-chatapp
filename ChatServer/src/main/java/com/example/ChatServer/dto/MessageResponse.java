package com.example.ChatServer.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageResponse {
    private Integer id;
    private Integer senderId;
    private String senderName;
    private Integer receiverId;
    private Integer groupId;
    private String content;
    private String messageType;
    private String status;
    private LocalDateTime createdAt;
}
