package com.example.chatapp.model;

import com.google.gson.annotations.SerializedName;

public class User {
    private Integer id;
    private String username;
    private String phoneNumber;

    @SerializedName(value = "displayName")
    private String displayName;

    @SerializedName(value = "avatar")
    private String avatar;

    private String friendshipStatus;

    // Constructor cũ
    public User(Integer id, String username, String displayName, String avatar) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.avatar = avatar;
    }

    public User() {}

    // Getter & Setter mới
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    // Các getter/setter cũ giữ nguyên...
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getDisplayName() {
        if (displayName != null && !displayName.isEmpty()) return displayName;
        if (username != null && !username.isEmpty()) return username;
        return "User " + id;
    }

    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getFriendshipStatus() { return friendshipStatus; }
    public void setFriendshipStatus(String status) { this.friendshipStatus = status; }
}