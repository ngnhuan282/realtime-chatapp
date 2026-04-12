package com.example.ChatServer.service;

import com.example.ChatServer.dto.response.UserResponse;
import com.example.ChatServer.entity.Friendship;
import com.example.ChatServer.repository.FriendshipRepository;
import com.example.ChatServer.repository.UserRepository;
import com.example.ChatServer.socket.FriendshipSocketNotifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class FriendService {
    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final FriendshipSocketNotifier friendshipSocketNotifier;

    public FriendService(FriendshipRepository friendshipRepository,
                         UserRepository userRepository,
                         FriendshipSocketNotifier friendshipSocketNotifier) {
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
        this.friendshipSocketNotifier = friendshipSocketNotifier;
    }

    public void sendFriendRequest(int senderId, int receiverId) {
        if (senderId == receiverId) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Khong the ket ban voi chinh minh");
        }

        var existing = friendshipRepository.findByUser1IdAndUser2Id(senderId, receiverId)
                .or(() -> friendshipRepository.findByUser1IdAndUser2Id(receiverId, senderId));

        if (existing.isPresent()) {
            Friendship f = existing.get();
            String status = f.getStatus() == null ? "" : f.getStatus().trim().toUpperCase();
            if ("ACCEPTED".equals(status)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Da la ban be");
            }

            // PENDING
            if (f.getUser1Id() != null && f.getUser1Id() == senderId) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Ban da gui loi moi truoc do");
            }
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ban dang co loi moi tu nguoi nay - hay chap nhan");
        }

        Friendship f = Friendship.builder()
                .user1Id(senderId)
                .user2Id(receiverId)
                .status("PENDING")
                .createdAt(System.currentTimeMillis())
                .build();
        friendshipRepository.save(f);
    }

    public void acceptFriendRequest(int senderId, int receiverId) {
        Friendship f = friendshipRepository.findByUser1IdAndUser2Id(senderId, receiverId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong tim thay loi moi"));

        f.setStatus("ACCEPTED");
        friendshipRepository.save(f);

        // Realtime notify cho cả 2 phía
        if (friendshipSocketNotifier != null) {
            friendshipSocketNotifier.notifyFriendshipAccepted(senderId, receiverId);
        }
    }

    public List<UserResponse> getPendingRequests(int userId) {
        // Lời mời đến: user2Id = userId, status = PENDING
        List<Friendship> pending = friendshipRepository.findByStatusAndUser2Id("PENDING", userId);
        List<UserResponse> senders = new ArrayList<>();

        for (Friendship f : pending) {
            Integer senderId = f.getUser1Id();
            if (senderId == null) continue;
            userRepository.findById(senderId).ifPresent(u ->
                    senders.add(new UserResponse(u.getId(), u.getUsername(), u.getDisplayName(), u.getPhoneNumber(), u.getAvatar()))
            );
        }
        return senders;
    }

    public List<UserResponse> getSentRequests(int userId) {
        // Lời mời đã gửi: user1Id = userId, status = PENDING
        List<Friendship> pending = friendshipRepository.findByStatusAndUser1Id("PENDING", userId);
        List<UserResponse> receivers = new ArrayList<>();

        for (Friendship f : pending) {
            Integer receiverId = f.getUser2Id();
            if (receiverId == null) continue;
            userRepository.findById(receiverId).ifPresent(u ->
                    receivers.add(new UserResponse(u.getId(), u.getUsername(), u.getDisplayName(), u.getPhoneNumber(), u.getAvatar()))
            );
        }
        return receivers;
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