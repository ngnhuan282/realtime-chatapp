package com.example.ChatServer.service;

import com.example.ChatServer.dto.ConversationDTO;
import com.example.ChatServer.dto.MessageDTO;
import com.example.ChatServer.entity.ChatGroup;
import com.example.ChatServer.entity.Message;
import com.example.ChatServer.entity.User;
import com.example.ChatServer.repository.MessageRepository;
import com.example.ChatServer.repository.UserRepository;
import com.example.ChatServer.repository.GroupRepository;
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
import java.util.UUID;
import java.util.logging.Logger;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

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
            if (msg.getGroupId() == null) {
                // 1. Xác định ID người bạn
                Integer friendId = msg.getSenderId().equals(userId) ? msg.getReceiverId() : msg.getSenderId();

                // 2. KIỂM TRA QUAN TRỌNG: Chỉ xử lý nếu là chat 1-1 (friendId không null)
                if (friendId != null) {
                    User friend = userRepository.findById(friendId).orElse(null);
                    if (friend != null) {
                        long unread = messageRepository.countUnreadMessages(friendId, userId);

                        conversations.add(new ConversationDTO(
                                friendId, null, false,
                                friend.getDisplayName(),
                                msg.getContent(),
                                msg.getTimestamp(),
                                unread
                        ));
                    }
                }
            } else {
                // XỬ LÝ CHAT NHÓM
                ChatGroup group = groupRepository.findById(msg.getGroupId()).orElse(null);
                if (group != null) {
                    // Tạm thời chưa đếm unread cho group, gán = 0
                    conversations.add(new ConversationDTO(
                            null, group.getId(), true, group.getGroupName(),
                            msg.getContent(), msg.getTimestamp(), 0L
                    ));
                }
            }
        }
        return conversations;
    }

    public ResponseEntity<String> storeFile(MultipartFile file) {
        return storeFileInternal(file, null);
    }

    public ResponseEntity<String> storeVideo(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("video/")) {
            return ResponseEntity.badRequest().body("Chi nhan file video");
        }
        return storeFileInternal(file, "video");
    }

    private ResponseEntity<String> storeFileInternal(MultipartFile file, String subFolder) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("File rong");
        }

        try {
            // 1. Tạo thư mục uploads bên trong ChatServer
            Path uploadPath = resolveChatServerUploadsPath();
            if (subFolder != null && !subFolder.isBlank()) {
                uploadPath = uploadPath.resolve(subFolder);
            }
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

            // 2. Tạo tên file duy nhất tránh trùng lặp
            String originalFileName = file.getOriginalFilename() == null ? "file" : Paths.get(file.getOriginalFilename()).getFileName().toString();
            String fileName = UUID.randomUUID() + "_" + originalFileName.replaceAll("[^a-zA-Z0-9._-]", "_");
            Path filePath = uploadPath.resolve(fileName);

            // 3. Lưu file vật lý
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            if (subFolder != null && !subFolder.isBlank()) {
                return ResponseEntity.ok(subFolder + "/" + fileName);
            }
            return ResponseEntity.ok(fileName);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Lỗi lưu file");
        }
    }

    private Path resolveChatServerUploadsPath() {
        Path currentDir = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();

        Path cursor = currentDir;
        while (cursor != null) {
            Path name = cursor.getFileName();
            if (name != null && "ChatServer".equalsIgnoreCase(name.toString())) {
                return cursor.resolve("uploads");
            }

            Path chatServerDir = cursor.resolve("ChatServer");
            if (Files.isDirectory(chatServerDir)) {
                return chatServerDir.resolve("uploads");
            }

            cursor = cursor.getParent();
        }

        // Fallback only when ChatServer folder cannot be detected
        return currentDir.resolve("uploads");
    }

    public List<Message> getGroupHistory(Integer groupId) {
        List<Message> messages = messageRepository.findByGroupIdOrderByTimestampAsc(groupId);
    
        // Quét qua từng tin nhắn, tra ID người gửi để lấy Tên và Avatar
        for (Message m : messages) {
            userRepository.findById(m.getSenderId()).ifPresent(user -> {
                m.setSenderName(user.getDisplayName() != null ? user.getDisplayName() : user.getUsername());
                m.setSenderAvatar(user.getAvatar());
            });
        }
        return messages;
    }

    public User getUserById(Integer userId) {
        if (userId == null)  return null;
        return userRepository.findById(userId).orElse(null);
    }
}