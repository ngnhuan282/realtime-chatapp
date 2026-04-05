package com.example.chatapp.network.socket;

import android.util.Log;
import com.example.chatapp.model.Message;
import com.google.gson.Gson;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketManager {
    private static SocketManager instance;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private MessageListener listener;
    private boolean isConnected = false;
    private boolean isReading = false; // Flag kiểm soát vòng lặp đọc
    private Integer myUserId;

    private static final String SERVER_IP = "10.0.2.2";
    private static final int SERVER_PORT = 8081;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Gson gson = new Gson();

    public interface MessageListener {
        void onMessageReceived(Message message);
    }

    public static synchronized SocketManager getInstance() {
        if (instance == null) instance = new SocketManager();
        return instance;
    }

    private SocketManager() {}

    public void setMyUserId(Integer userId) {
        this.myUserId = userId;
    }

    // Cập nhật listener mỗi khi vào Activity mới
    public void setListener(MessageListener listener) {
        this.listener = listener;
    }

    public void connect() {
        executor.execute(this::connectInternal);
    }

    private void connectInternal() {
        try {
            // Kiểm tra nếu socket cũ đã chết hoặc chưa tạo
            if (socket == null || socket.isClosed() || !socket.isConnected()) {
                Log.d("SocketManager", "Đang thử kết nối tới server...");
                socket = new Socket();
                socket.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT), 5000);
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                isConnected = true;

                // Gửi handshake ngay khi vừa kết nối xong
                sendHandshakeIfPossible();

                // Chỉ bắt đầu vòng lặp đọc nếu chưa có vòng lặp nào chạy
                if (!isReading) {
                    startReadingLoop();
                }
            } else {
                // Nếu vẫn đang kết nối tốt, chỉ cần gửi lại handshake để báo hiệu User đang online
                sendHandshakeIfPossible();
            }
        } catch (IOException e) {
            Log.e("SocketManager", "Lỗi kết nối Socket: " + e.getMessage());
            isConnected = false;
            isReading = false;
        }
    }

    private void startReadingLoop() {
        isReading = true;
        new Thread(() -> {
            try {
                String line;
                while (isConnected && (line = in.readLine()) != null) {
                    Log.d("SOCKET_RAW", "Dữ liệu nhận: " + line);
                    try {
                        Message msg = gson.fromJson(line, Message.class);
                        if (listener != null) {
                            listener.onMessageReceived(msg);
                        }
                    } catch (Exception e) {
                        Log.e("SocketManager", "Lỗi Parse JSON: " + e.getMessage());
                    }
                }
            } catch (IOException e) {
                Log.e("SocketManager", "Vòng lặp đọc bị ngắt: " + e.getMessage());
            } finally {
                isReading = false;
                isConnected = false;
                closeEverything();
            }
        }).start();
    }

    private void sendHandshakeIfPossible() {
        if (myUserId != null && out != null) {
            Message handshake = new Message(myUserId, 0, "Handshake", System.currentTimeMillis(), true);
            sendMessage(handshake);
            Log.d("SocketManager", "Đã gửi Handshake cho ID: " + myUserId);
        }
    }

    public void sendMessage(Message message) {
        executor.execute(() -> {
            if (out != null && !socket.isClosed()) {
                try {
                    String json = gson.toJson(message);
                    out.println(json);
                    out.flush();
                } catch (Exception e) {
                    Log.e("SocketManager", "Lỗi khi gửi tin: " + e.getMessage());
                }
            } else {
                Log.e("SocketManager", "Không thể gửi tin, Socket đã đóng.");
            }
        });
    }

    private void closeEverything() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}