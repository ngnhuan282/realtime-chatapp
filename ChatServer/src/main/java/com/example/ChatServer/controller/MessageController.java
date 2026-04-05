package com.example.ChatServer.controller;

import com.example.ChatServer.entity.Message;
import com.example.ChatServer.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    // API lấy lịch sử tin nhắn: GET /api/messages/history?u1=UserA&u2=UserB
    @GetMapping("/history")
    public ResponseEntity<List<Message>> getHistory(
            @RequestParam Integer u1,
            @RequestParam Integer u2) {
        List<Message> history = messageService.getChatHistory(u1, u2);
        return ResponseEntity.ok(history);
    }
}