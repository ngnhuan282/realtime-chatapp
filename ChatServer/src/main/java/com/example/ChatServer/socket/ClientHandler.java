package com.example.ChatServer.socket;

import com.example.ChatServer.entity.Message;
import com.example.ChatServer.service.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private MessageService messageService;
    private BufferedReader in;
    private PrintWriter out;

    // SỬA LỖI TẠI ĐÂY: Đổi String thành Integer để khớp với model Message
    private Integer currentUserId;

    private ObjectMapper objectMapper = new ObjectMapper();

    public ClientHandler(Socket socket, MessageService messageService) {
        this.socket = socket;
        this.messageService = messageService;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            String input;
            while ((input = in.readLine()) != null) {
                try {
                    // Chuyển JSON thành đối tượng Message (ID lúc này là Integer)
                    Message msg = objectMapper.readValue(input, Message.class);

                    // Bước 1: Handshake - Đăng ký định danh Socket
                    if (currentUserId == null) {
                        currentUserId = msg.getSenderId(); // Integer gán cho Integer -> Hết lỗi!
                        ConnectionManager.onlineUsers.put(currentUserId, this);
                        System.out.println("User " + currentUserId + " đã kết nối Socket.");
                        continue;
                    }

                    // Bước 2: Lưu vào Database MySQL [cite: 7, 22]
                    messageService.saveMessage(msg); // Đảm bảo MessageService nhận tham số là Message

                    // Bước 3: Chuyển tiếp Realtime cho người nhận [cite: 6, 24]
                    // ConnectionManager.onlineUsers bây giờ dùng Key là Integer
                    ClientHandler receiver = ConnectionManager.onlineUsers.get(msg.getReceiverId());
                    if (receiver != null) {
                        receiver.sendMessage(input);
                        System.out.println("Đã chuyển tiếp tin nhắn tới User: " + msg.getReceiverId());
                    }
                } catch (Exception e) {
                    System.err.println("JSON Error: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Mất kết nối với User: " + currentUserId);
        } finally {
            closeConnection();
        }
    }

    public void sendMessage(String jsonMessage) {
        if (out != null) out.println(jsonMessage);
    }

    private void closeConnection() {
        if (currentUserId != null) {
            ConnectionManager.onlineUsers.remove(currentUserId);
        }
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}