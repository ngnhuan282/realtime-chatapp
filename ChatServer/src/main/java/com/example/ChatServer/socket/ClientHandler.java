package com.example.ChatServer.socket;

import com.example.ChatServer.entity.GroupMember;
import com.example.ChatServer.entity.Message;
import com.example.ChatServer.repository.GroupMemberRepository;
import com.example.ChatServer.service.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final MessageService messageService;
    private final GroupMemberRepository groupMemberRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private BufferedReader in;
    private PrintWriter out;
    private Integer currentUserId;

    public ClientHandler(Socket socket, MessageService messageService, GroupMemberRepository groupMemberRepository) {
        this.socket = socket;
        this.messageService = messageService;
        this.groupMemberRepository = groupMemberRepository;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            String input;
            while ((input = in.readLine()) != null) {
                if (input.isEmpty()) continue;

                // Xóa BOM nếu có
                if (input.charAt(0) == '\uFEFF') {
                    input = input.substring(1);
                }

                Message msg = objectMapper.readValue(input, Message.class);

                // ==================== HANDSHAKE ====================
                if (currentUserId == null) {
                    if (msg.getReceiverId() != null && msg.getReceiverId() == 0) {
                        currentUserId = msg.getSenderId();
                        ConnectionManager.onlineUsers.put(currentUserId, this);
                        System.out.println("✅ User " + currentUserId + " đã kết nối Socket thành công.");
                        continue;
                    }
                    System.out.println("Tin đầu tiên không phải handshake.");
                    continue;
                }

                // ==================== XỬ LÝ TIN NHẮN ====================
                boolean isGroup = (msg.getGroupId() != null && msg.getGroupId() > 0);

                // 🔥 FIX QUAN TRỌNG: Group message phải có receiverId = null
                if (isGroup) {
                    msg.setReceiverId(null);
                }

                // Kiểm tra hợp lệ
                if (!isGroup && (msg.getReceiverId() == null || msg.getReceiverId() <= 0)) {
                    System.out.println("Tin nhắn không có receiverId hợp lệ");
                    continue;
                }

                System.out.println("Nhận tin từ User " + msg.getSenderId()
                        + (isGroup ? " (Nhóm " + msg.getGroupId() + ")" : " → " + msg.getReceiverId())
                        + " | Nội dung: " + msg.getContent());

                // Đính kèm thông tin người gửi
                com.example.ChatServer.entity.User sender = messageService.getUserById(msg.getSenderId());
                if (sender != null) {
                    msg.setSenderName(sender.getDisplayName() != null ? sender.getDisplayName() : sender.getUsername());
                    msg.setSenderAvatar(sender.getAvatar());
                }

                // Lưu vào DB (bây giờ receiverId đã là null nếu là group)
                messageService.saveMessage(msg);

                // ==================== FORWARD REALTIME ====================
                String jsonToForward = objectMapper.writeValueAsString(msg);

                if (isGroup) {
                    // Broadcast cho tất cả thành viên nhóm (trừ người gửi)
                    List<GroupMember> members = groupMemberRepository.findByGroupId(msg.getGroupId());
                    for (GroupMember gm : members) {
                        if (gm.getUser().getId().equals(currentUserId)) continue;

                        ClientHandler handler = ConnectionManager.onlineUsers.get(gm.getUser().getId());
                        if (handler != null) {
                            handler.sendMessage(jsonToForward);
                        }
                    }
                    System.out.println("✅ Đã forward tin nhóm " + msg.getGroupId() + " cho " + (members.size() - 1) + " thành viên");
                } else {
                    // Tin 1-1
                    ClientHandler receiverHandler = ConnectionManager.onlineUsers.get(msg.getReceiverId());
                    if (receiverHandler != null && receiverHandler != this) {
                        receiverHandler.sendMessage(jsonToForward);
                        System.out.println("✅ Đã forward tin 1-1 tới User: " + msg.getReceiverId());
                    } else {
                        System.out.println("⚠️ Người nhận chưa online: " + msg.getReceiverId());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi parse JSON: " + e.getMessage());
        }    finally {
            closeConnection();
        }
    }

    public void sendMessage(String jsonMessage) {
        if (out != null) {
            out.println(jsonMessage);
            out.flush();
            System.out.println("Đã gửi dữ liệu qua socket tới client");
        }
    }

    private void closeConnection() {
        if (currentUserId != null) {
            ConnectionManager.onlineUsers.remove(currentUserId);
            System.out.println("User " + currentUserId + " đã ngắt kết nối.");
        }
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}