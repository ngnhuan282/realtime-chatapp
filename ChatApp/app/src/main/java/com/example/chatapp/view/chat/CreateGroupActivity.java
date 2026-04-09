package com.example.chatapp.view.chat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.R;
import com.example.chatapp.adapter.UserSelectAdapter;
import com.example.chatapp.model.CreateGroupRequest;
import com.example.chatapp.model.GroupResponse;
import com.example.chatapp.model.User;
import com.example.chatapp.network.rest.ApiClient;
import com.example.chatapp.network.rest.FriendApi;
import com.example.chatapp.network.rest.MessageApi;
import com.example.chatapp.view.darkmode.BaseActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateGroupActivity extends BaseActivity {

    private EditText edtGroupName;
    private RecyclerView rvUsers;
    private Button btnCreate;

    private List<User> userList = new ArrayList<>();
    private UserSelectAdapter userAdapter;
    private Integer myUserId;

    private FriendApi friendApi;
    private MessageApi messageApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        myUserId = getSharedPreferences("ChatAppPrefs", MODE_PRIVATE)
                .getInt("myUserId", -1);

        friendApi = ApiClient.getClient().create(FriendApi.class);
        messageApi = ApiClient.getClient().create(MessageApi.class);

        initViews();
        setupRecyclerView();
        loadMyFriends();

        btnCreate.setOnClickListener(v -> createGroup());
    }

    private void initViews() {
        edtGroupName = findViewById(R.id.edtGroupName);
        rvUsers = findViewById(R.id.rvUsers);
        btnCreate = findViewById(R.id.btnCreate);
    }

    private void setupRecyclerView() {
        userAdapter = new UserSelectAdapter(this, userList);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        rvUsers.setAdapter(userAdapter);
    }

    private void loadMyFriends() {
        if (myUserId == -1) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        friendApi.getMyFriends(myUserId).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    userList.clear();
                    userList.addAll(response.body());
                    userAdapter.notifyDataSetChanged();

                    if (userList.isEmpty()) {
                        Toast.makeText(CreateGroupActivity.this,
                                "Bạn chưa có bạn bè nào để tạo nhóm", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(CreateGroupActivity.this,
                            "Không tải được danh sách bạn bè", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Toast.makeText(CreateGroupActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createGroup() {
        String groupName = edtGroupName.getText().toString().trim();
        if (groupName.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên nhóm", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Integer> selectedIds = userAdapter.getSelectedUserIds();
        if (selectedIds.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất 1 thành viên", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lọc bỏ null để tránh lỗi server
        selectedIds.removeIf(id -> id == null);

        if (!selectedIds.contains(myUserId)) {
            selectedIds.add(myUserId);
        }

        CreateGroupRequest request = new CreateGroupRequest(groupName, myUserId, selectedIds);

        messageApi.createGroup(request).enqueue(new Callback<GroupResponse>() {
            @Override
            public void onResponse(Call<GroupResponse> call, Response<GroupResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(CreateGroupActivity.this, "Tạo nhóm thành công!", Toast.LENGTH_SHORT).show();

                    // Quay về ChatListActivity và refresh
                    Intent intent = new Intent(CreateGroupActivity.this, ChatListActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(CreateGroupActivity.this, "Tạo nhóm thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GroupResponse> call, Throwable t) {
                Toast.makeText(CreateGroupActivity.this, "Lỗi kết nối server: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}