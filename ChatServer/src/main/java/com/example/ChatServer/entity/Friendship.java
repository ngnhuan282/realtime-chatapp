package com.example.ChatServer.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "friendships",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_friendship_pair", columnNames = {"user1Id", "user2Id"})
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer user1Id;
    private Integer user2Id;

    private long createdAt;
}
