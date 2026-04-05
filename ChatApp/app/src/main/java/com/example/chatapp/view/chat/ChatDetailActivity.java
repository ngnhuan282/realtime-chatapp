package com.example.chatapp.view.chat;

import android.content.Context;
import android.content.SharedPreferences;
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

    private Integer myUserId;    // ID của chính mình (Lấy từ SharedPreferences)
    private Integer friendId;    // ID người đang chat cùng (Lấy từ Intent)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        // 1. Lấy dữ liệu ID để định danh người gửi và người nhận
        SharedPreferences sharedPref = getSharedPreferences("ChatAppPrefs", Context.MODE_PRIVATE);
        myUserId = sharedPref.getInt("myUserId", -1);
        friendId = getIntent().getIntExtra("friendId", 2); // Mặc định là 2 (Alice) nếu test offline

        // 2. Ánh xạ các View
        rvMessages = findViewById(R.id.rvMessages);
        edtMessage = findViewById(R.id.edtMessage);
        btnSend = findViewById(R.id.btnSend);
        btnBack = findViewById(R.id.btnBack);

        // 3. Thiết lập RecyclerView hiển thị tin nhắn [cite: 36, 40]
        adapter = new ChatAdapter(messages);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        rvMessages.setAdapter(adapter);

        // 4. Kết nối Socket Realtime
        socketManager = new SocketManager();
        socketManager.connect(msg -> {
            runOnUiThread(() -> {
                // Kiểm tra nếu tin nhắn này dành cho cuộc hội thoại hiện tại
                messages.add(msg);
                adapter.notifyItemInserted(messages.size() - 1);
                rvMessages.scrollToPosition(messages.size() - 1); // UX: Tự động cuộn [cite: 42, 51]
            });
        });

        // 5. Xử lý sự kiện gửi tin nhắn [cite: 28, 40, 50]
        btnSend.setOnClickListener(v -> {
            String text = edtMessage.getText().toString().trim();
            if (!text.isEmpty() && myUserId != -1) {
                // Tạo đối tượng tin nhắn với kiểu Integer [cite: 85-91]
                Message newMsg = new Message(myUserId, friendId, text, System.currentTimeMillis(), true);

                // Cập nhật UI ngay lập tức [cite: 40]
                messages.add(newMsg);
                adapter.notifyItemInserted(messages.size() - 1);
                rvMessages.scrollToPosition(messages.size() - 1);

                // Gửi JSON qua Socket lên Server [cite: 28, 46]
                socketManager.sendMessage(newMsg);

                edtMessage.setText(""); // Xóa khung nhập [cite: 40]
            }
        });

        btnBack.setOnClickListener(v -> finish());
    }
}