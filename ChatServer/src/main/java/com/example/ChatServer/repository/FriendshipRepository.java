package com.example.ChatServer.repository;

import com.example.ChatServer.entity.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Integer> {
    boolean existsByUser1IdAndUser2Id(Integer user1Id, Integer user2Id);
}
