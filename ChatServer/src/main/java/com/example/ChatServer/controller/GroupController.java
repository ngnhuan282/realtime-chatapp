package com.example.ChatServer.controller;

import com.example.ChatServer.dto.request.CreateGroupRequest;
import com.example.ChatServer.dto.response.GroupResponse;
import com.example.ChatServer.entity.ChatGroup;
import com.example.ChatServer.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    @Autowired
    private GroupService groupService;

    @PostMapping("/create")
    public GroupResponse createGroup(@RequestBody CreateGroupRequest request) {
        return groupService.createGroup(request);
    }

    @GetMapping("/my-groups")
    public List<ChatGroup> getMyGroups(@RequestParam Integer userId) {
        return groupService.getMyGroups(userId);
    }
}