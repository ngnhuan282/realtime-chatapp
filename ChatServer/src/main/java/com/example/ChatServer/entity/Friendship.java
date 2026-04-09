package com.example.ChatServer.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "friendships", uniqueConstraints = {
        @UniqueConstraint(name = "uk_friendship_pair", columnNames = {"user1Id", "user2Id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Friendship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer user1Id; // Người gửi lời mời
    private Integer user2Id; // Người nhận lời mời

    @Column(columnDefinition = "VARCHAR(20) DEFAULT 'PENDING'")
    private String status; // "PENDING" hoặc "ACCEPTED"

    private long createdAt;
}