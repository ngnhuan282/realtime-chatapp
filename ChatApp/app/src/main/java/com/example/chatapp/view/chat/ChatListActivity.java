package com.example.chatapp.view.chat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.chatapp.R;
import com.example.chatapp.model.Conversation;
import com.example.chatapp.network.rest.ApiClient;
import com.example.chatapp.network.rest.MessageApi;
import com.example.chatapp.view.setting.SettingsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatListActivity extends AppCompatActivity {
    private RecyclerView rvChats;
    private ChatListAdapter adapter;
    private List<Conversation> conversations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        rvChats = findViewById(R.id.rvChats);
        rvChats.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ChatListAdapter(conversations, chat -> {
            Intent intent = new Intent(ChatListActivity.this, ChatDetailActivity.class);
            intent.putExtra("friendId", chat.getFriendId());
            intent.putExtra("friendName", chat.getDisplayName());
            startActivity(intent);
        });

        rvChats.setAdapter(adapter);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_chats) {
                return true;
            }
            else if (id == R.id.nav_calls) {
                return true;
            }
            else if (id == R.id.nav_people) {
                return true;
            }
            else if (id == R.id.nav_settings) {
                Intent intent = new Intent(ChatListActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });

        // Mặc định chọn Chats khi vào app
        bottomNav.setSelectedItemId(R.id.nav_chats);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchConversations(); // Cập nhật lại danh sách khi quay lại màn hình
    }

    private void fetchConversations() {
        Integer myId = getSharedPreferences("ChatAppPrefs", MODE_PRIVATE).getInt("myUserId", -1);
        if (myId == -1) return;

        MessageApi messageApi = ApiClient.getClient().create(MessageApi.class);
        messageApi.getConversations(myId).enqueue(new Callback<List<Conversation>>() {
            @Override
            public void onResponse(Call<List<Conversation>> call, Response<List<Conversation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    conversations.clear();
                    conversations.addAll(response.body());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Conversation>> call, Throwable t) {
                Log.e("ChatList", "Lỗi tải hội thoại: " + t.getMessage());
            }
        });
    }
}