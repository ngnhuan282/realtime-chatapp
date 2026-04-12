package com.example.ChatServer.controller;

import com.example.ChatServer.dto.response.UserResponse;
import com.example.ChatServer.service.FriendService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
public class FriendController {
    private final FriendService friendService;

    public FriendController(FriendService friendService) { this.friendService = friendService; }

    @PostMapping("/send-request")
    public ResponseEntity<String> send(@RequestParam int senderId, @RequestParam int receiverId) {
        friendService.sendFriendRequest(senderId, receiverId);
        return ResponseEntity.ok("Da gui loi moi");
    }

    @PostMapping("/accept-request")
    public ResponseEntity<String> accept(@RequestParam int senderId, @RequestParam int receiverId) {
        friendService.acceptFriendRequest(senderId, receiverId);
        return ResponseEntity.ok("Da ket ban thanh cong");
    }

    @GetMapping("/my-friends")
    public ResponseEntity<List<UserResponse>> getFriends(@RequestParam int userId) {
        return ResponseEntity.ok(friendService.getMyFriends(userId));
    }

    @GetMapping("/pending-requests")
    public ResponseEntity<List<UserResponse>> pendingRequests(@RequestParam int userId) {
        return ResponseEntity.ok(friendService.getPendingRequests(userId));
    }

    @GetMapping("/sent-requests")
    public ResponseEntity<List<UserResponse>> sentRequests(@RequestParam int userId) {
        return ResponseEntity.ok(friendService.getSentRequests(userId));
    }
}