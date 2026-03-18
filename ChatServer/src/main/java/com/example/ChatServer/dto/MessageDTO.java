package com.example.ChatServer.dto;

import com.example.ChatServer.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Getter
@Setter
public class MessageDTO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "senderId")
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiverId")
    private User receiver;

    @ManyToOne
    @JoinColumn(name = "groupId")
    private ChatGroup group;

    private String content;
    private String messageType;
    private String status;
    private LocalDateTime createdAt;
}
