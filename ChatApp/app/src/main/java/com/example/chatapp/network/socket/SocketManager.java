package com.example.chatapp.network.socket;

import android.util.Log;

import com.example.chatapp.model.Message;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class SocketManager {
    private static SocketManager instance;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private MessageListener listener;
    private FriendshipListener friendshipListener;
    private ConnectionListener connectionListener;
    private MessageStatusListener messageStatusListener;

    private volatile boolean isConnected = false;
    private volatile boolean isReading = false;
    private Integer myUserId;

    private static final String SERVER_IP = "10.0.2.2";
    private static final int SERVER_PORT = 8081;
    private static final long RECONNECT_INTERVAL_SECONDS = 4L;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final ScheduledExecutorService reconnectExecutor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> reconnectFuture;
    private final Queue<Message> pendingQueue = new ConcurrentLinkedQueue<>();
    private final Gson gson = new Gson();

    public interface MessageListener {
        void onMessageReceived(Message message);
    }

    public interface FriendshipListener {
        void onFriendshipAccepted(int userIdA, int userIdB);
    }

    public interface ConnectionListener {
        void onConnectionChanged(boolean connected);
    }

    public interface MessageStatusListener {
        void onMessageStatusChanged(String localId, String status);
    }

    public static synchronized SocketManager getInstance() {
        if (instance == null) instance = new SocketManager();
        return instance;
    }

    private SocketManager() {}

    public void setMyUserId(Integer userId) {
        this.myUserId = userId;
    }

    public void setListener(MessageListener listener) {
        this.listener = listener;
    }

    public void setFriendshipListener(FriendshipListener friendshipListener) {
        this.friendshipListener = friendshipListener;
    }

    public void setConnectionListener(ConnectionListener connectionListener) {
        this.connectionListener = connectionListener;
    }

    public void setMessageStatusListener(MessageStatusListener messageStatusListener) {
        this.messageStatusListener = messageStatusListener;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void connect() {
        executor.execute(this::connectInternal);
    }

    private void connectInternal() {
        try {
            if (canSendNow()) {
                sendHandshakeIfPossible();
                return;
            }

            Log.d("SocketManager", "Đang thử kết nối tới server...");
            closeEverything();

            socket = new Socket();
            socket.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT), 5000);
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            isConnected = true;
            notifyConnectionChanged(true);
            stopReconnectLoop();

            sendHandshakeIfPossible();
            flushPendingMessages();

            if (!isReading) {
                startReadingLoop();
            }
        } catch (IOException e) {
            Log.e("SocketManager", "Lỗi kết nối Socket: " + e.getMessage());
            onDisconnectedInternal();
            startReconnectLoop();
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

                        if (msg != null && "FRIENDSHIP".equalsIgnoreCase(msg.getMessageType())) {
                            String content = msg.getContent();
                            if (content != null && "ACCEPTED".equalsIgnoreCase(content.trim())) {
                                if (friendshipListener != null
                                        && msg.getSenderId() != null
                                        && msg.getReceiverId() != null) {
                                    friendshipListener.onFriendshipAccepted(msg.getSenderId(), msg.getReceiverId());
                                }
                            }
                        }

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
                onDisconnectedInternal();
                closeEverything();
                startReconnectLoop();
            }
        }).start();
    }

    public void sendFriendshipAcceptedEvent(int userIdA, int userIdB) {
        executor.execute(() -> {
            Message event = new Message(userIdA, userIdB, "ACCEPTED", System.currentTimeMillis(), true);
            event.setMessageType("FRIENDSHIP");
            sendRawMessage(event, false);
        });
    }

    private void sendHandshakeIfPossible() {
        if (myUserId != null && out != null) {
            Message handshake = new Message(myUserId, 0, "Handshake", System.currentTimeMillis(), true);
            handshake.setMessageType("SYSTEM");
            sendRawMessage(handshake, false);
            Log.d("SocketManager", "Đã gửi Handshake cho ID: " + myUserId);
        }
    }

    public void sendMessage(Message message) {
        executor.execute(() -> {
            if (message == null) {
                return;
            }

            if (!canSendNow()) {
                message.setStatus(Message.STATUS_SENDING);
                enqueueMessageIfNeeded(message);
                notifyMessageStatusChanged(message);
                startReconnectLoop();
                return;
            } else {
                sendRawMessage(message, true);
            }
        });
    }

    private void sendRawMessage(Message message, boolean trackStatus) {
        if (!canSendNow()) {
            if (trackStatus) {
                message.setStatus(Message.STATUS_SENDING);
                enqueueMessageIfNeeded(message);
                notifyMessageStatusChanged(message);
            }
            startReconnectLoop();
            return;
        }

        try {
            if (trackStatus) {
                message.setStatus(Message.STATUS_SENDING);
                notifyMessageStatusChanged(message);
            }

            // Persist SENT status on server; keep local UI status separate via callbacks.
            Message outbound = gson.fromJson(gson.toJson(message), Message.class);
            outbound.setStatus(Message.STATUS_SENT);

            String json = gson.toJson(outbound);
            out.println(json);
            out.flush();

            if (out.checkError()) {
                throw new IOException("Socket write failed");
            }

            if (trackStatus) {
                message.setStatus(Message.STATUS_SENT);
                notifyMessageStatusChanged(message);
            }
        } catch (Exception e) {
            Log.e("SocketManager", "Lỗi khi gửi tin: " + e.getMessage());
            onDisconnectedInternal();
            closeEverything();
            if (trackStatus) {
                message.setStatus(Message.STATUS_SENDING);
                enqueueMessageIfNeeded(message);
                notifyMessageStatusChanged(message);
            }
            startReconnectLoop();
        }
    }

    private void flushPendingMessages() {
        Message pending;
        while ((pending = pendingQueue.poll()) != null) {
            sendRawMessage(pending, true);
            if (!canSendNow()) {
                break;
            }
        }
    }

    private void enqueueMessageIfNeeded(Message message) {
        String localId = message.getLocalId();
        if (localId == null || localId.trim().isEmpty()) {
            pendingQueue.offer(message);
            return;
        }

        for (Message queued : pendingQueue) {
            if (localId.equals(queued.getLocalId())) {
                return;
            }
        }
        pendingQueue.offer(message);
    }

    private boolean canSendNow() {
        return isConnected && socket != null && socket.isConnected() && !socket.isClosed() && out != null;
    }

    private void startReconnectLoop() {
        if (reconnectFuture != null && !reconnectFuture.isDone()) {
            return;
        }

        reconnectFuture = reconnectExecutor.scheduleWithFixedDelay(
                this::connect,
                RECONNECT_INTERVAL_SECONDS,
                RECONNECT_INTERVAL_SECONDS,
                TimeUnit.SECONDS
        );
    }

    private void stopReconnectLoop() {
        if (reconnectFuture != null && !reconnectFuture.isDone()) {
            reconnectFuture.cancel(false);
        }
        reconnectFuture = null;
    }

    private void onDisconnectedInternal() {
        if (isConnected) {
            isConnected = false;
            notifyConnectionChanged(false);
        }
    }

    private void notifyConnectionChanged(boolean connected) {
        if (connectionListener != null) {
            connectionListener.onConnectionChanged(connected);
        }
    }

    private void notifyMessageStatusChanged(Message message) {
        if (messageStatusListener == null || message == null) {
            return;
        }
        String localId = message.getLocalId();
        if (localId != null) {
            messageStatusListener.onMessageStatusChanged(localId, message.getStatus());
        }
    }

    private void closeEverything() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
            in = null;
            out = null;
            socket = null;
        } catch (IOException e) {
            Log.e("SocketManager", "Lỗi đóng socket: " + e.getMessage());
        }
    }
}