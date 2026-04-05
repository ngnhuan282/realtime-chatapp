package com.example.chatapp.view.chat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.chatapp.R;
import com.example.chatapp.model.Message;
import com.example.chatapp.network.rest.ApiClient;
import com.example.chatapp.network.rest.MessageApi;
import com.example.chatapp.network.socket.SocketManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatDetailActivity extends AppCompatActivity {

    private ChatAdapter adapter;
    private List<Message> messages = new ArrayList<>();
    private SocketManager socketManager;
    private EditText edtMessage;
    private RecyclerView rvMessages;
    private FloatingActionButton btnSend;
    private ImageView btnBack;
    private TextView tvFriendName;

    private Integer myUserId;
    private Integer friendId;
    private String friendName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        // Nhận dữ liệu từ SharedPreferences và Intent
        SharedPreferences sharedPref = getSharedPreferences("ChatAppPrefs", Context.MODE_PRIVATE);
        myUserId = sharedPref.getInt("myUserId", -1);
        friendId = getIntent().getIntExtra("friendId", -1);
        friendName = getIntent().getStringExtra("friendName");

        initViews();
        setupRecyclerView();

        // Cấu hình Socket
        socketManager = SocketManager.getInstance();
        socketManager.setMyUserId(myUserId);

        // Gán listener để nhận tin
        socketManager.setListener(msg -> {
            runOnUiThread(() -> {
                // Chỉ hiển thị nếu tin nhắn đó thuộc về cuộc hội thoại này
                if (msg.getSenderId() != null &&
                        (msg.getSenderId().equals(friendId) || msg.getSenderId().equals(myUserId))) {

                    msg.setMe(msg.getSenderId().equals(myUserId));
                    messages.add(msg);
                    adapter.notifyItemInserted(messages.size() - 1);
                    rvMessages.scrollToPosition(messages.size() - 1);
                }
            });
        });

        // Luôn gọi connect để đảm bảo Socket đang sống và đã gửi Handshake
        socketManager.connect();

        loadHistory();

        btnSend.setOnClickListener(v -> {
            String text = edtMessage.getText().toString().trim();
            if (!text.isEmpty() && myUserId != -1 && friendId != -1) {
                Message newMsg = new Message(myUserId, friendId, text, System.currentTimeMillis(), true);

                // Hiển thị local trước cho mượt UI
                messages.add(newMsg);
                adapter.notifyItemInserted(messages.size() - 1);
                rvMessages.scrollToPosition(messages.size() - 1);

                socketManager.sendMessage(newMsg);
                edtMessage.setText("");
            }
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private void initViews() {
        rvMessages = findViewById(R.id.rvMessages);
        edtMessage = findViewById(R.id.edtMessage);
        btnSend = findViewById(R.id.btnSend);
        btnBack = findViewById(R.id.btnBack);
        tvFriendName = findViewById(R.id.tvFriendName);
        if (friendName != null) tvFriendName.setText(friendName);
    }

    private void setupRecyclerView() {
        adapter = new ChatAdapter(messages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        // Tự động cuộn xuống cuối khi load
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(adapter);
    }

    private void loadHistory() {
        if (myUserId == -1 || friendId == -1) return;
        MessageApi messageApi = ApiClient.getClient().create(MessageApi.class);
        messageApi.getHistory(myUserId, friendId).enqueue(new Callback<List<Message>>() {
            @Override
            public void onResponse(Call<List<Message>> call, Response<List<Message>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    messages.clear();
                    for (Message m : response.body()) {
                        m.setMe(m.getSenderId().equals(myUserId));
                        messages.add(m);
                    }
                    adapter.notifyDataSetChanged();
                    if (!messages.isEmpty()) {
                        rvMessages.scrollToPosition(messages.size() - 1);
                    }
                }
            }
            @Override
            public void onFailure(Call<List<Message>> call, Throwable t) {
                Log.e("ChatDetail", "Lỗi load history: " + t.getMessage());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Xóa listener khi Activity bị hủy để tránh rò rỉ bộ nhớ
        if (socketManager != null) {
            socketManager.setListener(null);
        }
    }
}