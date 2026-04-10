package com.example.chatapp.model;

import com.google.gson.annotations.SerializedName;
public class Conversation {
    private Integer friendId;
    private Integer groupId;
    
    @SerializedName(value = "group", alternate = {"isGroup"})
    private boolean isGroup;
    
    private String displayName;
    private String lastMessage;
    private long lastTime; 
    private int unreadCount;

    public Conversation() {}

    // Các Getters / Setters
    public Integer getFriendId() { return friendId; }
    public void setFriendId(Integer friendId) { this.friendId = friendId; }

    public Integer getGroupId() { return groupId; }
    public void setGroupId(Integer groupId) { this.groupId = groupId; }

    public boolean isGroup() { return isGroup; }
    public void setGroup(boolean group) { isGroup = group; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public long getLastTime() { return lastTime; }
    public void setLastTime(long lastTime) { this.lastTime = lastTime; }

    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }
}