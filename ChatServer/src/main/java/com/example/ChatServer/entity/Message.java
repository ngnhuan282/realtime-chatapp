package com.example.ChatServer.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "messages")
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "senderId")
    private Integer senderId;

    @Column(name = "receiverId")
    private Integer receiverId;
    private Integer groupId;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String messageType = "TEXT";
    private String status = "SENT";

    @Column(name = "createdAt")
    private long timestamp;
}