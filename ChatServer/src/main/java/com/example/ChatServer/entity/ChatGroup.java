package com.example.ChatServer.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "chatGroups")
@Data
public class ChatGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String groupName;

    @ManyToOne
    @JoinColumn(name = "createdBy")
    private User creator;

    @Column(insertable = false, updatable = false)
    private LocalDateTime createdAt;
}