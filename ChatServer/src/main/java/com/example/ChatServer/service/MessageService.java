package com.example.ChatServer.service;

import com.example.ChatServer.dto.MessageDTO;
import com.example.ChatServer.entity.Message;
import com.example.ChatServer.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

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
}