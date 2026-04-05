package com.example.chatapp.model;

import com.google.gson.annotations.SerializedName;

public class User {
    private Integer id;
    private String username;

    @SerializedName(value = "displayName")
    private String displayName;

    public User(Integer id, String username, String displayName) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
    }

    public User() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // Thêm fallback để an toàn
    public String getDisplayName() {
        if (displayName != null && !displayName.isEmpty()) {
            return displayName;
        }
        if (username != null && !username.isEmpty()) {
            return username;
        }
        return "User " + id;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
