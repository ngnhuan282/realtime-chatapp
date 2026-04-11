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

         // Truy vấn lấy tin nhắn 1-1 mới nhất
        @Query(value = "SELECT * FROM messages WHERE id IN (" +
            "  SELECT MAX(id) FROM messages " +
            "  WHERE (senderId = :userId OR receiverId = :userId) AND groupId IS NULL " +
            "  GROUP BY CASE WHEN senderId = :userId THEN receiverId ELSE senderId END " +
            ") ORDER BY createdAt DESC", nativeQuery = true)
        List<Message> findLatest1To1Messages(@Param("userId") Integer userId);

        // Truy vấn lấy tin nhắn Nhóm mới nhất
        @Query(value = "SELECT * FROM messages WHERE id IN (" +
            "  SELECT MAX(m.id) FROM messages m " +
            "  INNER JOIN groupMembers gm ON m.groupId = gm.groupId " +
            "  WHERE gm.userId = :userId " +
            "  GROUP BY m.groupId" +
            ") ORDER BY createdAt DESC", nativeQuery = true)
        List<Message> findLatestGroupMessages(@Param("userId") Integer userId);

        // Đếm số tin nhắn chưa đọc từ một người bạn gửi cho mình
        @Query("SELECT COUNT(m) FROM Message m WHERE m.senderId = :friendId " +
                        "AND m.receiverId = :myId AND m.status != 'READ'")
        long countUnreadMessages(@Param("friendId") Integer friendId, @Param("myId") Integer myId);

        List<Message> findByGroupIdOrderByTimestampAsc(Integer groupId);
}