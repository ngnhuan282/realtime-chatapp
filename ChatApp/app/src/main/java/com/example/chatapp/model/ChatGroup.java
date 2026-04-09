package com.example.chatapp.model;

import java.util.List;

public class ChatGroup {
    private Integer id;
    private String groupName;
    private Integer creatorId;        // thay vì User creator để đơn giản
    private List<User> members;
    private long createdAt;
    private String lastMessage;       // thêm để hiển thị ở danh sách chat
    private long lastTime;            // timestamp tin cuối

    // Getters & Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public Integer getCreatorId() { return creatorId; }
    public void setCreatorId(Integer creatorId) { this.creatorId = creatorId; }

    public List<User> getMembers() { return members; }
    public void setMembers(List<User> members) { this.members = members; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public long getLastTime() { return lastTime; }
    public void setLastTime(long lastTime) { this.lastTime = lastTime; }
}