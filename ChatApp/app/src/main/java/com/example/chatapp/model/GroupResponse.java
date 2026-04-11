package com.example.chatapp.model;

import com.google.gson.annotations.SerializedName;

public class GroupResponse {
    @SerializedName("id")
    private Integer groupId;
    
    private String groupName;
    private String message; // thông báo từ server

    // Getters & Setters
    public Integer getGroupId() { return groupId; }
    public void setGroupId(Integer groupId) { this.groupId = groupId; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}