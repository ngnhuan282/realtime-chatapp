package com.example.ChatServer.service;

import com.example.ChatServer.dto.response.UserResponse;
import com.example.ChatServer.entity.Friendship;
import com.example.ChatServer.repository.FriendshipRepository;
import com.example.ChatServer.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class FriendService {
    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    public FriendService(FriendshipRepository friendshipRepository, UserRepository userRepository) {
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
    }

    public boolean sendFriendRequest(int senderId, int receiverId) {
        if (senderId == receiverId) return false;

        // Kiểm tra xem đã có lời mời chưa (1 trong 2 phía gửi)
        if (friendshipRepository.existsByUser1IdAndUser2Id(senderId, receiverId) ||
                friendshipRepository.existsByUser1IdAndUser2Id(receiverId, senderId)) {
            return false;
        }

        Friendship f = Friendship.builder()
                .user1Id(senderId)
                .user2Id(receiverId)
                .status("PENDING")
                .createdAt(System.currentTimeMillis())
                .build();
        friendshipRepository.save(f);
        return true;
    }

    public boolean acceptFriendRequest(int senderId, int receiverId) {
        // Tìm lời mời mà senderId gửi cho receiverId (người nhấn nút chấp nhận)
        return friendshipRepository.findByUser1IdAndUser2Id(senderId, receiverId)
                .map(f -> {
                    f.setStatus("ACCEPTED");
                    friendshipRepository.save(f);
                    return true;
                }).orElse(false);
    }

    public List<UserResponse> getMyFriends(int userId) {
        // Chỉ lấy những người có status là ACCEPTED
        List<Friendship> friendships = friendshipRepository.findByStatusAndUser1IdOrUser2Id("ACCEPTED", userId, userId);
        List<UserResponse> friends = new ArrayList<>();

        for (Friendship f : friendships) {
            int friendId = (f.getUser1Id() == userId) ? f.getUser2Id() : f.getUser1Id();
            userRepository.findById(friendId).ifPresent(u ->
                    friends.add(new UserResponse(u.getId(), u.getUsername(), u.getDisplayName(), u.getPhoneNumber(), u.getAvatar()))
            );
        }
        return friends;
    }
}