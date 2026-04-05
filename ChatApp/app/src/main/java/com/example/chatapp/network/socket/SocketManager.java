package com.example.chatapp.network.socket;

import android.util.Log;
import com.example.chatapp.model.Message;
import com.google.gson.Gson;
import java.io.*;
import java.net.Socket;

public class SocketManager {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private static final String SERVER_IP = "10.0.2.2"; // IP mặc định của localhost trên Android Emulator
    private static final int SERVER_PORT = 8080;

    public interface MessageListener {
        void onMessageReceived(Message message);
    }

    public void connect(MessageListener listener) {
        new Thread(() -> {
            try {
                socket = new Socket(SERVER_IP, SERVER_PORT);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String line;
                while ((line = in.readLine()) != null) {
                    Message msg = new Gson().fromJson(line, Message.class);
                    msg.setMe(false); // Tin nhắn từ Socket luôn là nhận từ người khác
                    listener.onMessageReceived(msg);
                }
            } catch (IOException e) {
                Log.e("SocketManager", "Connection error", e);
            }
        }).start();
    }

    public void sendMessage(Message message) {
        new Thread(() -> {
            if (out != null) {
                String json = new Gson().toJson(message);
                out.println(json);
            }
        }).start();
    }
}
