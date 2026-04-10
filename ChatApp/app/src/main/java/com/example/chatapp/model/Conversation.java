package com.example.chatapp.model;

import com.google.gson.annotations.SerializedName;
public class Conversation {
    private Integer friendId;
    private Integer groupId;
    
    @SerializedName("group")
    private boolean isGroup;
    
    private String displayName;
    private String lastMessage;
    private long lastTime; 
    private int unreadCount;

    // Các Getters / Setters
    public Integer getFriendId() { return friendId; }
    public Integer getGroupId() { return groupId; }
    public boolean isGroup() { return isGroup; }
    public String getDisplayName() { return displayName; }
    public String getLastMessage() { return lastMessage; }
    public long getLastTime() { return lastTime; }
    public int getUnreadCount() { return unreadCount; }
}