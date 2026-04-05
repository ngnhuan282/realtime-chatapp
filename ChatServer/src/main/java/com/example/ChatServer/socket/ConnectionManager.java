package com.example.ChatServer.socket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    // Map lưu trữ: Key = userId, Value = Luồng xử lý của người đó
    public static Map<String, ClientHandler> onlineUsers = new ConcurrentHashMap<>();
}