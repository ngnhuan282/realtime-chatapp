package com.example.ChatServer.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "messages")
@Data
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer senderId;
    private Integer receiverId;
    private Integer groupId;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String messageType = "TEXT";
    private String status = "SENT";
    private long timestamp;
}