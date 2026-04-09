package com.example.ChatServer.service;

import com.example.ChatServer.dto.request.CreateGroupRequest;
import com.example.ChatServer.dto.response.GroupResponse;
import com.example.ChatServer.dto.response.UserResponse;
import com.example.ChatServer.entity.ChatGroup;
import com.example.ChatServer.entity.GroupMember;
import com.example.ChatServer.entity.User;
import com.example.ChatServer.repository.GroupMemberRepository;
import com.example.ChatServer.repository.GroupRepository;
import com.example.ChatServer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupService {

    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private GroupMemberRepository groupMemberRepository;
    @Autowired
    private UserRepository userRepository;

    public GroupResponse createGroup(CreateGroupRequest request) {
        User creator = userRepository.findById(request.getCreatorId())
                .orElseThrow(() -> new RuntimeException("Creator not found"));

        ChatGroup group = new ChatGroup();
        group.setGroupName(request.getGroupName());
        group.setCreator(creator);
        group.setCreatedAt(System.currentTimeMillis());
        group = groupRepository.save(group);

        // Thêm creator
        addMember(group, creator.getId());

        // Thêm thành viên khác
        for (Integer memberId : request.getMemberIds()) {
            if (!memberId.equals(request.getCreatorId())) {
                addMember(group, memberId);
            }
        }

        List<UserResponse> members = groupMemberRepository.findByGroupId(group.getId()).stream()
                .map(gm -> new UserResponse(
                        gm.getUser().getId(),
                        gm.getUser().getUsername(),
                        gm.getUser().getDisplayName(),
                        gm.getUser().getAvatar()))
                .collect(Collectors.toList());

        return new GroupResponse(
                group.getId(),
                group.getGroupName(),
                new UserResponse(creator.getId(), creator.getUsername(), creator.getDisplayName(), null),
                members,
                LocalDateTime.now());
    }

    private void addMember(ChatGroup group, Integer userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
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