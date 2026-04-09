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
        return friendService.sendFriendRequest(senderId, receiverId)
                ? ResponseEntity.ok("Đã gửi lời mời") : ResponseEntity.badRequest().body("Lỗi gửi lời mời");
    }

    @PostMapping("/accept-request")
    public ResponseEntity<String> accept(@RequestParam int senderId, @RequestParam int receiverId) {
        return friendService.acceptFriendRequest(senderId, receiverId)
                ? ResponseEntity.ok("Đã kết bạn thành công") : ResponseEntity.badRequest().body("Không tìm thấy lời mời");
    }

    @GetMapping("/my-friends")
    public ResponseEntity<List<UserResponse>> getFriends(@RequestParam int userId) {
        return ResponseEntity.ok(friendService.getMyFriends(userId));
    }
}