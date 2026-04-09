package com.example.ChatServer.controller;

import com.example.ChatServer.service.FriendService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/friends")
public class FriendController {

    private final FriendService friendService;

    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    
    @PostMapping("/send-request")
    public ResponseEntity<?> addFriend(@RequestParam int senderId, @RequestParam int receiverId) {
        boolean created = friendService.addFriend(senderId, receiverId);
        return ResponseEntity.ok(created ? "Đã kết bạn" : "Đã là bạn");
    }


    @PostMapping("/accept-request")
    public ResponseEntity<?> acceptRequest(@RequestParam int requestId) {
        return ResponseEntity.ok("OK");
    }
}
