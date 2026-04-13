package com.example.ChatServer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConversationDTO {
    private Integer friendId; // Sẽ là null nếu đây là nhóm
    private Integer groupId;  // Sẽ là null nếu đây là chat 1-1
    private boolean isGroup;
    private String displayName;
    private String avatar; // Avatar của friend (null nếu là nhóm)
    private String lastMessage;
    private long lastTime;
    private long unreadCount;
}
