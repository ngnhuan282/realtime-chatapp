package com.example.ChatServer.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

import com.example.ChatServer.entity.GroupMember;
import com.example.ChatServer.entity.Message;
import com.example.ChatServer.repository.GroupMemberRepository;
import com.example.ChatServer.service.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientHandler implements Runnable {

    private Socket socket;
    private MessageService messageService;
    private GroupMemberRepository groupMemberRepository;
    private BufferedReader in;
    private PrintWriter out;

    private Integer currentUserId;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
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
                try {
                    // Some clients may prefix UTF-8 BOM (\uFEFF). Strip it to avoid JSON parse errors.
                    if (!input.isEmpty() && input.charAt(0) == '\uFEFF') {
                        input = input.substring(1);
                    }

                    // Parse tin nhắn từ client
                    Message msg = objectMapper.readValue(input, Message.class);

                    // ==================== HANDSHAKE ====================
                    if (currentUserId == null) {
                        if (msg.getReceiverId() != null && msg.getReceiverId() == 0) {
                            currentUserId = msg.getSenderId();
                            ConnectionManager.onlineUsers.put(currentUserId, this);
                            System.out.println("✅ User " + currentUserId + " đã kết nối Socket thành công.");
                            continue;
                        } else {
                            System.out.println("Tin đầu tiên không phải handshake.");
                            continue;
                        }
                    }

                    // ==================== TIN NHẮN THẬT ====================
                    boolean isPrivate = (msg.getReceiverId() != null && msg.getReceiverId() > 0);
                    boolean isGroup = (msg.getGroupId() != null && msg.getGroupId() > 0);

                    if (!isPrivate && !isGroup) {
                        System.out.println("Tin nhắn không có receiverId hợp lệ");
                        continue;
                    }

                    System.out.println("Nhận tin từ User " + msg.getSenderId()
                            + " → " + msg.getReceiverId()
                            + " | Nội dung: " + msg.getContent());

                          
                    // Đính kèm tên người gửi trước khi forward đi cho người khác
                    com.example.ChatServer.entity.User sender = messageService.getUserById(msg.getSenderId());
                    if (sender != null) {
                        msg.setSenderName(sender.getDisplayName() != null ? sender.getDisplayName() : sender.getUsername());
                        msg.setSenderAvatar(sender.getAvatar());
                    }

                    // Lưu vào Database
                    messageService.saveMessage(msg);

                    // Forward realtime cho người nhận
                    ClientHandler receiverHandler = ConnectionManager.onlineUsers.get(msg.getReceiverId());
                    if (receiverHandler != null && receiverHandler != this) {
                        // Gửi nguyên JSON nhận được (để giữ nguyên định dạng)
                        receiverHandler.sendMessage(input);
                        System.out.println("✅ Đã forward tin nhắn tới User: " + msg.getReceiverId());
                    } else if (receiverHandler == null) {
                        System.out.println("⚠️ Người nhận chưa online hoặc chưa handshake: " + msg.getReceiverId());
                    }

                    

                    // Chuyển lại thành chuỗi JSON đã có kèm senderName
                    String jsonToForward = objectMapper.writeValueAsString(msg);

                    // FORWARD REALTIME
                    if (isGroup) {
                        // TIN NHẮN NHÓM
                        List<GroupMember> members = groupMemberRepository.findByGroupId(msg.getGroupId());
                        for (GroupMember gm : members) {
                            // Không gửi lại cho chính mình (vì client đã hiển thị rồi)
                            if (gm.getUser().getId().equals(currentUserId)) continue;

                            ClientHandler handler = ConnectionManager.onlineUsers.get(gm.getUser().getId());
                            if (handler != null) {
                                handler.sendMessage(jsonToForward);
                            }
                        }
                        System.out.println("✅ Đã forward tin nhóm " + msg.getGroupId());
                    } else {
                        // TIN NHẮN 1-1
                        ClientHandler receiver = ConnectionManager.onlineUsers.get(msg.getReceiverId());
                        if (receiver != null) {
                            receiver.sendMessage(jsonToForward);
                            System.out.println("✅ Đã forward tin 1-1 tới User: " + msg.getReceiverId());
                        }
                }

                } catch (Exception e) {
                    System.err.println("Lỗi parse JSON: " + e.getMessage() + " | Input: " + input);
                }
            }
        } catch (IOException e) {
            System.err.println("Mất kết nối với User: " + currentUserId);
        } finally {
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