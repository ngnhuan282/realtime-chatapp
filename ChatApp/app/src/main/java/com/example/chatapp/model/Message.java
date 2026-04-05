package com.example.chatapp.model;

public class Message {
    private Integer senderId;
    private Integer receiverId;
    private String content;
    private long timestamp;
    private boolean isMe;

    public Message() {}

    public Message(Integer senderId, Integer receiverId, String content, long timestamp, boolean isMe) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.timestamp = timestamp;
        this.isMe = isMe;
    }

    // Getters & Setters
    public Integer getSenderId() { return senderId; }
    public void setSenderId(Integer senderId) { this.senderId = senderId; }

    public Integer getReceiverId() { return receiverId; }
    public void setReceiverId(Integer receiverId) { this.receiverId = receiverId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isMe() { return isMe; }
    public void setMe(boolean me) { isMe = me; }
}