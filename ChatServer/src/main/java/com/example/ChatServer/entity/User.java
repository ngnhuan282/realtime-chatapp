package com.example.ChatServer.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String username;
    private String password;
    private String displayName;
    private String avatar;
    private String phoneNumber;

    @Column(insertable = false, updatable = false)
    private long createdAt;
}