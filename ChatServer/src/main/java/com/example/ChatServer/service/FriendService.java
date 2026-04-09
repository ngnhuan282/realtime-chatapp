package com.example.ChatServer.service;

import com.example.ChatServer.entity.Friendship;
import com.example.ChatServer.repository.FriendshipRepository;
import com.example.ChatServer.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class FriendService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    public FriendService(FriendshipRepository friendshipRepository, UserRepository userRepository) {
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
    }

    public boolean addFriend(int userId, int friendId) {
        if (userId <= 0 || friendId <= 0) {
            throw new IllegalArgumentException("userId/friendId không hợp lệ");
        }
        if (userId == friendId) {
            throw new IllegalArgumentException("Không thể tự kết bạn với chính mình");
        }
        if (!userRepository.existsById(userId) || !userRepository.existsById(friendId)) {
            throw new IllegalArgumentException("Không tìm thấy user");
        }

        int u1 = Math.min(userId, friendId);
        int u2 = Math.max(userId, friendId);

        if (friendshipRepository.existsByUser1IdAndUser2Id(u1, u2)) {
            return false;
        }

        Friendship friendship = Friendship.builder()
                .user1Id(u1)
                .user2Id(u2)
                .createdAt(System.currentTimeMillis())
                .build();

        friendshipRepository.save(friendship);
        return true;
    }
}
