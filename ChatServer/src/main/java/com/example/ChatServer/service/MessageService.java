package com.example.ChatServer.service;

import com.example.ChatServer.dto.ConversationDTO;
import com.example.ChatServer.dto.MessageDTO;
import com.example.ChatServer.entity.ChatGroup;
import com.example.ChatServer.entity.Message;
import com.example.ChatServer.entity.User;
import com.example.ChatServer.entity.Friendship;
import com.example.ChatServer.repository.FriendshipRepository;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    @Autowired
    private FriendshipRepository friendshipRepository;

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
        // 1. Lấy riêng lẻ 2 loại tin nhắn
        List<Message> latest1To1 = messageRepository.findLatest1To1Messages(userId);
        List<Message> latestGroup = messageRepository.findLatestGroupMessages(userId);

        // 2. Gộp lại và sắp xếp theo thời gian mới nhất (DESC)
        List<Message> allLatest = new ArrayList<>();
        allLatest.addAll(latest1To1);
        allLatest.addAll(latestGroup);
        allLatest.sort((m1, m2) -> Long.compare(m2.getTimestamp(), m1.getTimestamp()));

        List<ConversationDTO> conversations = new ArrayList<>();
        Set<Integer> friendIdsInConversation = new HashSet<>();

        for (Message msg : allLatest) {
        if (msg.getGroupId() == null) {
            Integer friendId = msg.getSenderId().equals(userId) ? msg.getReceiverId() : msg.getSenderId();
            if (friendId != null) {
                User friend = userRepository.findById(friendId).orElse(null);
                if (friend != null) {
                    long unread = messageRepository.countUnreadMessages(friendId, userId);
                    friendIdsInConversation.add(friendId);
                    conversations.add(new ConversationDTO(
                            friendId, null, false, friend.getDisplayName(),
                            friend.getAvatar(),
                            msg.getContent(), msg.getTimestamp(), unread
                    ));
                }
            }
        } else {
            ChatGroup group = groupRepository.findById(msg.getGroupId()).orElse(null);
            if (group != null) {
                conversations.add(new ConversationDTO(
                        null, group.getId(), true, group.getGroupName(),
                        null,
                        msg.getContent(), msg.getTimestamp(), 0L
                ));
            }
        }
    }

        // 3. Bổ sung hội thoại từ danh sách bạn bè ACCEPTED nhưng chưa có tin nhắn
        // -> để người dùng bấm vào nhắn tin ngay trong DS chat
        if (friendshipRepository != null) {
            List<Friendship> friendships = friendshipRepository.findFriendshipsByStatusForUser("ACCEPTED", userId);
            for (Friendship f : friendships) {
                if (f == null) continue;
                Integer u1 = f.getUser1Id();
                Integer u2 = f.getUser2Id();
                if (u1 == null || u2 == null) continue;

                Integer friendId = u1.equals(userId) ? u2 : u1;
                if (friendId == null || friendIdsInConversation.contains(friendId)) continue;

                User friend = userRepository.findById(friendId).orElse(null);
                if (friend == null) continue;

                conversations.add(new ConversationDTO(
                        friendId, null, false,
                        friend.getDisplayName(),
                        friend.getAvatar(),
                        "Các bạn hiện đã trở thành bạn bè",
                        f.getCreatedAt(),
                        0L
                ));
                friendIdsInConversation.add(friendId);
            }
        }

        // 4. Sắp xếp lại theo thời gian mới nhất
        conversations.sort((c1, c2) -> Long.compare(c2.getLastTime(), c1.getLastTime()));

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