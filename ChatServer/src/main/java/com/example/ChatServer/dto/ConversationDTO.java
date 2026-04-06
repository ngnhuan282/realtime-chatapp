package com.example.ChatServer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConversationDTO {
    private Integer friendId;
    private String displayName;
    private String lastMessage;
    private long lastTime; // Dùng timestamp để Android tự format
    private long unreadCount;
}
