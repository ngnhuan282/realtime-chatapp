package com.example.chatapp.view.chat;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.chatapp.R;
import com.example.chatapp.model.Message;
import com.example.chatapp.network.socket.SocketManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class ChatDetailActivity extends AppCompatActivity {
    private ChatAdapter adapter;
    private List<Message> messages = new ArrayList<>();
    private SocketManager socketManager;
    private EditText edtMessage;
    private RecyclerView rvMessages;
    private FloatingActionButton btnSend;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        // 1. Ánh xạ các View từ activity_chat_detail.xml
        rvMessages = findViewById(R.id.rvMessages);
        edtMessage = findViewById(R.id.edtMessage);
        btnSend = findViewById(R.id.btnSend);
        btnBack = findViewById(R.id.btnBack);

        // 2. Thiết lập RecyclerView
        adapter = new ChatAdapter(messages);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        rvMessages.setAdapter(adapter);

        // 3. Kết nối Socket để nhận tin nhắn realtime
        socketManager = new SocketManager();
        socketManager.connect(msg -> {
            runOnUiThread(() -> {
                messages.add(msg);
                adapter.notifyItemInserted(messages.size() - 1);
                rvMessages.scrollToPosition(messages.size() - 1); // Cuộn xuống tin mới
            });
        });

        // 4. Xử lý sự kiện gửi tin nhắn
        btnSend.setOnClickListener(v -> {
            String text = edtMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                // Tạo tin nhắn mới (isMe = true)
                Message newMsg = new Message("Me", "Friend", text, System.currentTimeMillis(), true);

                messages.add(newMsg);
                adapter.notifyItemInserted(messages.size() - 1);
                rvMessages.scrollToPosition(messages.size() - 1);

                // Gửi qua socket
                socketManager.sendMessage(newMsg);

                // Xóa nội dung sau khi gửi
                edtMessage.setText("");
            }
        });

        // 5. Nút quay lại
        btnBack.setOnClickListener(v -> finish());
    }
}