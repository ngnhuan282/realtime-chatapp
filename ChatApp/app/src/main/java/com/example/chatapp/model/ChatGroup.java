package com.example.chatapp.model;

import java.util.List;

public class ChatGroup {
    private Integer id;
    private String groupName;
    private User creator;
    private List<User> members;
    private long createdAt;

    // Getters & Setters
    public Integer getId() { return id; }
    public String getGroupName() { return groupName; }
    public List<User> getMembers() { return members; }
}