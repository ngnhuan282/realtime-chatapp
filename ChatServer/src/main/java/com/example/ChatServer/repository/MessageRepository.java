package com.example.ChatServer.repository;

import com.example.ChatServer.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Integer> {
    // Lấy lịch sử tin nhắn giữa 2 người dùng kiểu Integer
    List<Message> findBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderByTimestampAsc(
            Integer s1, Integer r1, Integer s2, Integer r2);
}