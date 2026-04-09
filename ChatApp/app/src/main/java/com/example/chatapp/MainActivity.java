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
import com.example.chatapp.network.rest.ApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> userList = new ArrayList<>();
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Chú ý: Đảm bảo layout này có chứa RecyclerView với ID là recyclerViewUsers
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. Ánh xạ RecyclerView và thiết lập LayoutManager
        recyclerView = findViewById(R.id.recyclerViewUsers);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }

        // 2. Khởi tạo ApiService
        apiService = ApiClient.getClient().create(ApiService.class);

        // 3. Giả sử ID người dùng hiện tại (Lấy từ Login thành công hoặc SharedPreferences)
        int myCurrentUserId = 1;

        // 4. Tải danh sách người dùng
        loadUsers(myCurrentUserId);
    }

    private void loadUsers(int myId) {
        apiService.getAllUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    userList = response.body();

                    // Khởi tạo và set Adapter
                    userAdapter = new UserAdapter(MainActivity.this, userList, myId, apiService);
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