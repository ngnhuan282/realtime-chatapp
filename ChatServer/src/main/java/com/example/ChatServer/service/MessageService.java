package com.example.ChatServer.service;

import com.example.ChatServer.dto.ConversationDTO;
import com.example.ChatServer.dto.MessageDTO;
import com.example.ChatServer.entity.Message;
import com.example.ChatServer.entity.User;
import com.example.ChatServer.repository.MessageRepository;
import com.example.ChatServer.repository.UserRepository;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 1. Lưu tin nhắn trực tiếp từ đối tượng Entity (Dùng cho Socket)
     */
    public void saveMessage(Message message) {
        messageRepository.save(message);
    }

    /**
     * 2. Lưu tin nhắn từ DTO (Dùng cho REST API hoặc logic cần chuyển đổi)
     */
    public void saveMessageFromDTO(MessageDTO dto) {
        Message entity = new Message();
        entity.setSenderId(dto.getSenderId());
        entity.setReceiverId(dto.getReceiverId());
        entity.setGroupId(dto.getGroupId());
        entity.setContent(dto.getContent());
        entity.setTimestamp(dto.getTimestamp());
        entity.setMessageType(dto.getMessageType());

        messageRepository.save(entity);
    }

    /**
     * 3. Lấy lịch sử chat giữa 2 người (Sử dụng Integer cho đồng bộ với DB)
     */
    public List<Message> getChatHistory(Integer user1, Integer user2) {
        return messageRepository.findBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderByTimestampAsc(
                user1, user2, user2, user1);
    }

    public List<ConversationDTO> getConversationList(Integer userId) {
        List<Message> latestMessages = messageRepository.findLatestMessagesByUser(userId);
        List<ConversationDTO> conversations = new ArrayList<>();

        for (Message msg : latestMessages) {
            // 1. Xác định ID người bạn
            Integer friendId = msg.getSenderId().equals(userId) ? msg.getReceiverId() : msg.getSenderId();

            // 2. KIỂM TRA QUAN TRỌNG: Chỉ xử lý nếu là chat 1-1 (friendId không null)
            if (friendId != null) {
                User friend = userRepository.findById(friendId).orElse(null);
                if (friend != null) {
                    long unread = messageRepository.countUnreadMessages(friendId, userId);

                    conversations.add(new ConversationDTO(
                            friendId,
                            friend.getDisplayName(),
                            msg.getContent(),
                            msg.getTimestamp(),
                            unread
                    ));
                }
            } else {
                // Đây là tin nhắn nhóm, tạm thời bỏ qua hoặc xử lý logic Chat Nhóm tại đây
                System.out.println("Updating...");
            }
        }
        return conversations;
    }

    public ResponseEntity<String> storeFile(MultipartFile file) {
        try {
            // 1. Tạo thư mục 'uploads' nếu chưa có
            Path uploadPath = Paths.get("uploads");
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

            // 2. Tạo tên file duy nhất tránh trùng lặp
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);

            // 3. Lưu file vật lý
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return ResponseEntity.ok(fileName);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Lỗi lưu file");
        }
    }
}