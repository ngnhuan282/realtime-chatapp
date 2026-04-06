package com.example.chatapp.model;

public class Conversation {
    private Integer friendId;
    private String displayName;
    private String lastMessage;
    private long lastTime; // Server trả về timestamp
    private int unreadCount;

    // Constructor, Getters và Setters
    public Conversation(Integer friendId, String displayName, String lastMessage, long lastTime, int unreadCount) {
        this.friendId = friendId;
        this.displayName = displayName;
        this.lastMessage = lastMessage;
        this.lastTime = lastTime;
        this.unreadCount = unreadCount;
    }

    public Integer getFriendId() { return friendId; }
    public String getDisplayName() { return displayName; }
    public String getLastMessage() { return lastMessage; }
    public long getLastTime() { return lastTime; }
    public int getUnreadCount() { return unreadCount; }
}