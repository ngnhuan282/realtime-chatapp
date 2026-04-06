package com.example.ChatServer.repository;

import com.example.ChatServer.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Integer> {
    // Lấy lịch sử tin nhắn giữa 2 người dùng kiểu Integer
    List<Message> findBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderByTimestampAsc(
            Integer s1, Integer r1, Integer s2, Integer r2);

    // Truy vấn lấy danh sách tin nhắn mới nhất của mỗi cuộc hội thoại 1-1
    @Query(value = "SELECT * FROM messages WHERE id IN (" +
            "  SELECT MAX(id) FROM messages " +
            "  WHERE senderId = :userId OR receiverId = :userId " +
            "  GROUP BY CASE " +
            "    WHEN senderId = :userId THEN receiverId " +
            "    ELSE senderId END" +
            ") ORDER BY createdAt DESC", nativeQuery = true)
    List<Message> findLatestMessagesByUser(@Param("userId") Integer userId);

    // Đếm số tin nhắn chưa đọc từ một người bạn gửi cho mình
    @Query("SELECT COUNT(m) FROM Message m WHERE m.senderId = :friendId " +
            "AND m.receiverId = :myId AND m.status != 'READ'")
    long countUnreadMessages(@Param("friendId") Integer friendId, @Param("myId") Integer myId);
}