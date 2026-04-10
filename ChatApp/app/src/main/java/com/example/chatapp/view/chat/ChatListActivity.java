package com.example.chatapp.view.chat;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.adapter.ChatListAdapter;
import com.example.chatapp.adapter.OnConversationClickListener;
import com.example.chatapp.model.Conversation;
import com.example.chatapp.network.rest.ApiClient;
import com.example.chatapp.network.rest.MessageApi;
import com.example.chatapp.network.socket.SocketManager;
import com.example.chatapp.view.darkmode.BaseActivity;
import com.example.chatapp.view.people.PeopleActivity;
import com.example.chatapp.view.setting.SettingsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatListActivity extends BaseActivity {

    private RecyclerView rvChats;
    private ChatListAdapter adapter;
    private List<Conversation> conversations = new ArrayList<>();
    private ImageView imgProfile;
    private FloatingActionButton fabAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        imgProfile = findViewById(R.id.imgProfile);
        bindMyAvatar();

        rvChats = findViewById(R.id.rvChats);
        rvChats.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ChatListAdapter(conversations, new OnConversationClickListener() {
            @Override
            public void onItemClick(Conversation conversation) {
                Intent intent = new Intent(ChatListActivity.this, ChatDetailActivity.class);
                if (conversation.isGroup()) {
                    intent.putExtra("groupId", conversation.getGroupId());
                    intent.putExtra("groupName", conversation.getDisplayName());
                } else {
                    intent.putExtra("friendId", conversation.getFriendId());
                    intent.putExtra("friendName", conversation.getDisplayName());
                }
                startActivity(intent);
            }
        });

        rvChats.setAdapter(adapter);

        // Xử lý nút + tạo nhóm
        fabAdd = findViewById(R.id.fabAdd);
        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> {
                Intent intent = new Intent(ChatListActivity.this, CreateGroupActivity.class);
                startActivity(intent);
            });
        }

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_chats) {
                return true;
            } else if (id == R.id.nav_people) {
                Intent intent = new Intent(ChatListActivity.this, PeopleActivity.class);
                startActivity(intent);
                finish();           // Optional: finish để refresh khi quay lại
                return true;
            } else if (id == R.id.nav_settings) {
                Intent intent = new Intent(ChatListActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_calls) {
                return true;
            }
            return false;
        });

        bottomNav.setSelectedItemId(R.id.nav_chats);
    }

    private void bindMyAvatar() {
        String myName = getSharedPreferences("ChatAppPrefs", MODE_PRIVATE).getString("myDisplayName", "Me");
        String avatar = getSharedPreferences("ChatAppPrefs", MODE_PRIVATE).getString("myAvatar", null);

        String url = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            url = (avatar == null || avatar.trim().isEmpty()) ?
                    "https://ui-avatars.com/api/?name=" + URLEncoder.encode(myName, StandardCharsets.UTF_8) + "&size=128" :
                    avatar;
        }

        Glide.with(this)
                .load(url)
                .placeholder(R.drawable.avatar_placeholder)
                .error(R.drawable.avatar_placeholder)
                .circleCrop()
                .into(imgProfile);
    }

    @Override
    protected void onResume() {
        super.onResume();
    
        // 1. Lấy API để load danh sách chat từ DB (load lần đầu)
        fetchConversations();

        // 2. Đăng ký nhận tin nhắn Real-time qua Socket để tự động Refresh
        Integer myId = getSharedPreferences("ChatAppPrefs", MODE_PRIVATE).getInt("myUserId", -1);
        if (myId != -1) {
            SocketManager socketManager = SocketManager.getInstance();
            socketManager.setMyUserId(myId);
            socketManager.connect(); // Nếu đã connect, nó sẽ chỉ gửi lại handshake
        
            socketManager.setListener(msg -> {
                // Bất cứ khi nào có tin nhắn mới (kể cả SYSTEM tạo nhóm hay người khác nhắn tin tới)
                // Ta chỉ cần gọi lại fetchConversations() để update danh sách mới nhất
                runOnUiThread(() -> fetchConversations());
            });
        }
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