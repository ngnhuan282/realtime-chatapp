package com.example.ChatServer.socket;

import com.example.ChatServer.entity.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class FriendshipSocketNotifier {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void notifyFriendshipAccepted(int userIdA, int userIdB) {
        Message event = new Message();
        event.setSenderId(userIdA);
        event.setReceiverId(userIdB);
        event.setContent("ACCEPTED");
        event.setMessageType("FRIENDSHIP");
        event.setTimestamp(System.currentTimeMillis());
        event.setStatus("SENT");

        String json;
        try {
            json = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            return;
        }

        ClientHandler handlerA = ConnectionManager.onlineUsers.get(userIdA);
        if (handlerA != null) {
            handlerA.sendMessage(json);
        }

        ClientHandler handlerB = ConnectionManager.onlineUsers.get(userIdB);
        if (handlerB != null && handlerB != handlerA) {
            handlerB.sendMessage(json);
        }
    }
}
