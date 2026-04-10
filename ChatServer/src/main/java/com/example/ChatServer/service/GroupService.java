package com.example.ChatServer.service;

import com.example.ChatServer.dto.request.CreateGroupRequest;
import com.example.ChatServer.dto.response.GroupResponse;
import com.example.ChatServer.dto.response.UserResponse;
import com.example.ChatServer.entity.ChatGroup;
import com.example.ChatServer.entity.GroupMember;
import com.example.ChatServer.entity.User;
import com.example.ChatServer.entity.Message;
import com.example.ChatServer.repository.GroupMemberRepository;
import com.example.ChatServer.repository.GroupRepository;
import com.example.ChatServer.repository.UserRepository;
import com.example.ChatServer.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.ChatServer.socket.ClientHandler;
import com.example.ChatServer.socket.ConnectionManager;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupService {

    @Autowired private GroupRepository groupRepository;
    @Autowired private GroupMemberRepository groupMemberRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private MessageRepository messageRepository;
    @Autowired private ObjectMapper objectMapper; 

    public GroupResponse createGroup(CreateGroupRequest request) {
        if (request.getCreatorId() == null) {
            throw new RuntimeException("Creator ID is required");
        }

        User creator = userRepository.findById(request.getCreatorId())
                .orElseThrow(() -> new RuntimeException("Creator not found with id: " + request.getCreatorId()));

        ChatGroup group = new ChatGroup();
        group.setGroupName(request.getGroupName());
        group.setCreator(creator);
        group.setCreatedAt(System.currentTimeMillis());
        group = groupRepository.save(group);

        // Thêm creator
        addMember(group, creator.getId());

        // Thêm thành viên khác - LỌC NULL
        if (request.getMemberIds() != null) {
            for (Integer memberId : request.getMemberIds()) {
                if (memberId != null && !memberId.equals(creator.getId())) {
                    addMember(group, memberId);
                }
            }
        }

        // 1. Tạo tin nhắn hệ thống (SYSTEM)
        Message sysMsg = new Message();
        sysMsg.setSenderId(creator.getId());
        sysMsg.setGroupId(group.getId());
        sysMsg.setContent(creator.getDisplayName() + " đã tạo nhóm " + group.getGroupName());
        sysMsg.setMessageType("SYSTEM");
        sysMsg.setStatus("SENT");
        sysMsg.setTimestamp(System.currentTimeMillis());
        sysMsg = messageRepository.save(sysMsg); // Lưu vào DB để có ID

        // 2. BẮN SOCKET REALTIME CHO CÁC THÀNH VIÊN ĐANG ONLINE
        try {
            // Chuyển Message thành JSON String giống luồng ClientHandler
            String jsonMsg = objectMapper.writeValueAsString(sysMsg);

            // Gửi cho người tạo (nếu họ đang online bằng thiết bị khác)
            ClientHandler creatorHandler = ConnectionManager.onlineUsers.get(creator.getId());
            if (creatorHandler != null) {
                creatorHandler.sendMessage(jsonMsg);
            }

            // Gửi cho các thành viên trong nhóm
            if (request.getMemberIds() != null) {
                for (Integer memberId : request.getMemberIds()) {
                    if (memberId != null && !memberId.equals(creator.getId())) {
                        ClientHandler memberHandler = ConnectionManager.onlineUsers.get(memberId);
                        if (memberHandler != null) {
                            memberHandler.sendMessage(jsonMsg);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi gửi Socket khi tạo nhóm: " + e.getMessage());
        }

        // Trả về response
        List<UserResponse> members = groupMemberRepository.findByGroupId(group.getId()).stream()
                .map(gm -> new UserResponse(
                        gm.getUser().getId(),
                        gm.getUser().getUsername(),
                        gm.getUser().getDisplayName() != null ? gm.getUser().getDisplayName() : gm.getUser().getUsername(),
                        gm.getUser().getPhoneNumber(),
                        gm.getUser().getAvatar()
                ))
                .collect(Collectors.toList());

        return new GroupResponse(
                group.getId(),
                group.getGroupName(),
                new UserResponse(creator.getId(), creator.getUsername(), creator.getDisplayName(), creator.getAvatar()),
                members,
                LocalDateTime.now()
        );
    }

    private void addMember(ChatGroup group, Integer userId) {
        if (userId == null) return;
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        boolean exists = groupMemberRepository.findByGroupId(group.getId()).stream()
                .anyMatch(gm -> gm.getUser().getId().equals(userId));

        if (!exists) {
            GroupMember gm = new GroupMember();
            gm.setGroup(group);
            gm.setUser(user);
            groupMemberRepository.save(gm);
        }
    }

    public List<ChatGroup> getMyGroups(Integer userId) {
        return groupRepository.findMyGroups(userId);
    }
}