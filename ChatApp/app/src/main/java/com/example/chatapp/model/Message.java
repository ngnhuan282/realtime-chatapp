package com.example.chatapp.model;

import com.google.gson.annotations.SerializedName;

public class Message {
    private Integer id;                    // Thêm field này
    private Integer senderId;
    private Integer receiverId;
    private Integer groupId;

    private String content;

    @SerializedName("messageType")
    private String messageType = "TEXT";

    private String status = "SENT";

    private long timestamp;

    private transient boolean isMe;        // transient = không serialize

    // Constructor rỗng (bắt buộc cho Gson)
    public Message() {}

    // Constructor dùng khi gửi tin nhắn từ client
    public Message(Integer senderId, Integer receiverId, String content, long timestamp, boolean isMe) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.timestamp = timestamp;
        this.isMe = isMe;
        this.messageType = "TEXT";
        this.status = "SENT";
    }

    // Getters & Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getSenderId() { return senderId; }
    public void setSenderId(Integer senderId) { this.senderId = senderId; }

    public Integer getReceiverId() { return receiverId; }
    public void setReceiverId(Integer receiverId) { this.receiverId = receiverId; }

    public Integer getGroupId() { return groupId; }
    public void setGroupId(Integer groupId) { this.groupId = groupId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isMe() { return isMe; }
    public void setMe(boolean me) { this.isMe = me; }
}