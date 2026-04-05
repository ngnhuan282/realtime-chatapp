package com.example.ChatServer.repository;

import com.example.ChatServer.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    // Lấy lịch sử tin nhắn giữa 2 người
    List<Message> findBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderByTimestampAsc(
            String s1, String r1, String s2, String r2);
}