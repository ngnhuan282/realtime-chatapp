package com.example.ChatServer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserResponse {

    private Integer id;
    private String username;
    private String displayName;
    private String avatar;

    // Constructor thủ công (để an toàn và rõ ràng)
    public UserResponse(Integer id, String username, String displayName, String avatar) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.avatar = avatar;
    }

    public UserResponse(Integer id, String username, String displayName, String phoneNumber, String avatar) {
    }
}