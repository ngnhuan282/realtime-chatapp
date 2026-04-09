package com.example.chatapp.network.rest;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    private Integer id;
    private String username;

    @SerializedName(value = "displayName")   // camelCase
    private String displayName;

    @SerializedName(value = "display_name")  // snake_case (thường gặp ở Spring Boot)
    private String displayNameSnake;

    private String message;

    @SerializedName(value = "avatar")
    private String avatar;

    public LoginResponse() {}

    // Getter an toàn - ưu tiên displayName, sau đó display_name, cuối cùng là username
    public String getDisplayName() {
        if (displayName != null && !displayName.isEmpty()) {
            return displayName;
        }
        if (displayNameSnake != null && !displayNameSnake.isEmpty()) {
            return displayNameSnake;
        }
        return username != null ? username : "User";
    }

    public Integer getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }

    public String getAvatar() {
        return avatar;
    }

    // Setters (nếu cần)
    public void setId(Integer id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setMessage(String message) { this.message = message; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
}