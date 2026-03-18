package com.example.ChatServer.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "groupMembers")
@Data
public class GroupMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "groupId")
    private ChatGroup group;

    @ManyToOne
    @JoinColumn(name = "userId")
    private User user;
}