package com.example.chatapp.model;

import java.util.List;

public class CreateGroupRequest {
    private String groupName;
    private Integer creatorId;
    private List<Integer> memberIds;

    public CreateGroupRequest() {}

    public CreateGroupRequest(String groupName, Integer creatorId, List<Integer> memberIds) {
        this.groupName = groupName;
        this.creatorId = creatorId;
        this.memberIds = memberIds;
    }

    // Getters & Setters
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public Integer getCreatorId() { return creatorId; }
    public void setCreatorId(Integer creatorId) { this.creatorId = creatorId; }

    public List<Integer> getMemberIds() { return memberIds; }
    public void setMemberIds(List<Integer> memberIds) { this.memberIds = memberIds; }
}