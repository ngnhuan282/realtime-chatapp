package com.example.chatapp.controller;
@RestController
@RequestMapping("/api/friends")
public class FriendController {
    @Autowired private FriendService friendService;

    @PostMapping("/send-request")
    public ResponseEntity<?> sendRequest(@RequestParam int senderId, @RequestParam int receiverId) {
        friendService.sendFriendRequest(senderId, receiverId);
        return ResponseEntity.ok("Đã gửi lời mời");
    }

    @PostMapping("/accept-request")
    public ResponseEntity<?> acceptRequest(@RequestParam int requestId) {
        friendService.acceptFriendRequest(requestId);
        return ResponseEntity.ok("Đã trở thành bạn bè");
    }
}