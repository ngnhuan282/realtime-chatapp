package com.example.chatapp;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.adapter.UserAdapter;
import com.example.chatapp.model.User;
import com.example.chatapp.network.rest.ApiClient;
import com.example.chatapp.network.rest.FriendApi;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> userList = new ArrayList<>();
    private FriendApi friendApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_people);   // Bạn đang dùng layout này để test

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.rvUsers);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }

        friendApi = ApiClient.getClient().create(FriendApi.class);
        int myCurrentUserId = 1;   // Thay bằng ID thật từ SharedPreferences sau

        // Set rỗng vì đây chỉ là test
        Set<Integer> emptySentSet = new HashSet<>();
        Set<Integer> emptyFriendsSet = new HashSet<>();
        Set<Integer> emptyIncomingSet = new HashSet<>();

        loadUsers(myCurrentUserId, emptySentSet, emptyFriendsSet, emptyIncomingSet);
    }

    private void loadUsers(int myId, Set<Integer> sentSet, Set<Integer> friendSet, Set<Integer> incomingSet) {
        friendApi.getAllUsers(myId).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    userList.clear();
                    userList.addAll(response.body());

                    // Sửa ở đây: truyền đủ 5 tham số
                    userAdapter = new UserAdapter(MainActivity.this, userList, myId, friendApi, sentSet, friendSet, incomingSet);
                    if (recyclerView != null) {
                        recyclerView.setAdapter(userAdapter);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Không thể lấy dữ liệu: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {
                Toast.makeText(MainActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}