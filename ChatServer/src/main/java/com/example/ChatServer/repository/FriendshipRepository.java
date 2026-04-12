package com.example.ChatServer.repository;

import com.example.ChatServer.entity.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Integer> {
    // Kiểm tra tồn tại quan hệ giữa 2 người
    boolean existsByUser1IdAndUser2Id(Integer u1, Integer u2);

    // Tìm bản ghi cụ thể để cập nhật status
    Optional<Friendship> findByUser1IdAndUser2Id(Integer senderId, Integer receiverId);

    // Lấy danh sách đã là bạn bè (ACCEPTED)
    List<Friendship> findByStatusAndUser1IdOrUser2Id(String status, Integer u1, Integer u2);

    // Lấy danh sách lời mời đến (PENDING): user2Id là người nhận lời mời
    List<Friendship> findByStatusAndUser2Id(String status, Integer user2Id);

    // Lấy danh sách lời mời đã gửi (PENDING): user1Id là người gửi lời mời
    List<Friendship> findByStatusAndUser1Id(String status, Integer user1Id);
}