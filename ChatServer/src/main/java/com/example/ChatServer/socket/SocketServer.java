package com.example.ChatServer.socket;

import com.example.ChatServer.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

@Component
public class SocketServer {
    @Autowired
    private MessageService messageService;

    private static final int PORT = 8081; // Port riêng cho Socket

    @EventListener(ApplicationReadyEvent.class)
    public void startSocketServer() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                System.out.println("Socket Server is running on port " + PORT);
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    // Mỗi khách kết nối sẽ có 1 thread riêng xử lý
                    new Thread(new ClientHandler(clientSocket, messageService)).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}