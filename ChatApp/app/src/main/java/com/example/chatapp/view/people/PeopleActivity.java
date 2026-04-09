package com.example.chatapp.view.people;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.R;
import com.example.chatapp.adapter.UserAdapter;
import com.example.chatapp.model.User;
import com.example.chatapp.network.rest.ApiClient;
import com.example.chatapp.network.rest.ApiService;
import com.example.chatapp.view.chat.ChatListActivity;
import com.example.chatapp.view.darkmode.BaseActivity;
import com.example.chatapp.view.setting.SettingsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.bumptech.glide.Glide;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PeopleActivity extends BaseActivity {

    private RecyclerView rvUsers;
    private ImageView imgProfile;
    private UserAdapter userAdapter;
    private final List<User> userList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_people);

        rvUsers = findViewById(R.id.rvUsers);
        imgProfile = findViewById(R.id.imgProfile);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));

        Integer myId = getSharedPreferences("ChatAppPrefs", MODE_PRIVATE).getInt("myUserId", -1);
        if (myId == -1) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        userAdapter = new UserAdapter(this, userList, myId, apiService);
        rvUsers.setAdapter(userAdapter);

        bindMyAvatar();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_people) {
                return true;
            } else if (id == R.id.nav_chats) {
                Intent intent = new Intent(PeopleActivity.this, ChatListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                return true;
            } else if (id == R.id.nav_settings) {
                Intent intent = new Intent(PeopleActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_calls) {
                return true;
            }
            return false;
        });
        bottomNav.setSelectedItemId(R.id.nav_people);

        loadUsers(apiService, myId);
    }

    private void bindMyAvatar() {
        String myName = getSharedPreferences("ChatAppPrefs", MODE_PRIVATE).getString("myDisplayName", "Me");
        String avatar = getSharedPreferences("ChatAppPrefs", MODE_PRIVATE).getString("myAvatar", null);

        String url = avatar;
        if (url == null || url.trim().isEmpty()) {
            url = "https://ui-avatars.com/api/?name=" +
                    URLEncoder.encode(myName == null ? "Me" : myName, StandardCharsets.UTF_8) +
                    "&size=128";
        }

        Glide.with(this)
                .load(url)
                .placeholder(R.drawable.avatar_placeholder)
                .error(R.drawable.avatar_placeholder)
                .circleCrop()
                .into(imgProfile);
    }

    private void loadUsers(ApiService apiService, int myId) {
        apiService.getAllUsers(myId).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    userList.clear();
                    userList.addAll(response.body());
                    userAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(PeopleActivity.this, "Không thể lấy danh sách user: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {
                Log.e("PeopleActivity", "Lỗi tải users", t);
                Toast.makeText(PeopleActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
