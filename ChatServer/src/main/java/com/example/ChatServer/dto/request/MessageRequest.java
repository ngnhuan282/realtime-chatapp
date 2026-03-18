package com.example.ChatServer.dto.request;

public class MessageRequest {
    private Integer senderId;
    private Integer receiverId; // Null nếu là chat nhóm
    private Integer groupId;    // Null nếu là chat 1-1
    private String content;
    private String messageType;
}
