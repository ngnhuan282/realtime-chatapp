package com.example.chatapp.view.people;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.adapter.UserAdapter;
import com.example.chatapp.model.User;
import com.example.chatapp.network.rest.ApiClient;
import com.example.chatapp.network.rest.FriendApi;
import com.example.chatapp.network.rest.UserApi;
import com.example.chatapp.network.socket.SocketManager;
import com.example.chatapp.view.chat.ChatListActivity;
import com.example.chatapp.view.darkmode.BaseActivity;
import com.example.chatapp.view.setting.SettingsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PeopleActivity extends BaseActivity {

    private RecyclerView rvUsers;
    private EditText edtSearchPhone;
    private TextView tvEmptyState;
    private ImageView imgProfile;

    private UserAdapter userAdapter;
    private final List<User> userList = new ArrayList<>();
    private Integer myUserId;

    private UserApi userApi;
    private FriendApi friendApi;

    private Set<Integer> sentRequestUserIds = new HashSet<>();
    private final Set<Integer> friendUserIds = new HashSet<>();
    private final Set<Integer> incomingRequestUserIds = new HashSet<>();
    private static final String PREF_SENT_REQUESTS = "sent_friend_requests";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_people);

        myUserId = getSharedPreferences("ChatAppPrefs", MODE_PRIVATE).getInt("myUserId", -1);
        if (myUserId == -1) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userApi = ApiClient.getClient().create(UserApi.class);
        friendApi = ApiClient.getClient().create(FriendApi.class);

        loadSentRequestsFromPrefs();

        fetchMyFriends();
        fetchIncomingRequests();
        fetchSentRequests();

        initViews();
        setupRecyclerView();
        bindMyAvatar();
        setupSearch();
        setupBottomNavigation();

        showEmptyState("Nhập số điện thoại để tìm kiếm bạn bè");
    }

    private void loadSentRequestsFromPrefs() {
        SharedPreferences prefs = getSharedPreferences(PREF_SENT_REQUESTS, MODE_PRIVATE);
        String saved = prefs.getString("sent_ids", "");
        if (!saved.isEmpty()) {
            String[] ids = saved.split(",");
            for (String idStr : ids) {
                try {
                    sentRequestUserIds.add(Integer.parseInt(idStr.trim()));
                } catch (Exception ignored) {}
            }
        }
    }

    private void saveSentRequestsToPrefs() {
        SharedPreferences prefs = getSharedPreferences(PREF_SENT_REQUESTS, MODE_PRIVATE);
        StringBuilder sb = new StringBuilder();
        for (Integer id : sentRequestUserIds) {
            if (sb.length() > 0) sb.append(",");
            sb.append(id);
        }
        prefs.edit().putString("sent_ids", sb.toString()).apply();
    }

    private void initViews() {
        rvUsers = findViewById(R.id.rvUsers);
        edtSearchPhone = findViewById(R.id.edtSearchPhone);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        imgProfile = findViewById(R.id.imgProfile);
    }

    private void setupRecyclerView() {
        userAdapter = new UserAdapter(this, userList, myUserId, friendApi, sentRequestUserIds, friendUserIds, incomingRequestUserIds);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        rvUsers.setAdapter(userAdapter);
    }

    private void setupSearch() {
        edtSearchPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String phone = s.toString().trim();
                if (phone.length() >= 9) {
                    searchUserByPhone(phone);
                } else {
                    showEmptyState("Nhập số điện thoại để tìm kiếm bạn bè");
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void searchUserByPhone(String phoneNumber) {
        userApi.searchByPhone(phoneNumber).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User found = response.body();

                    // Local override để UI đúng ngay cả khi backend chưa trả friendshipStatus trong user.
                    if (found.getId() != null) {
                        int foundId = found.getId();
                        if (friendUserIds.contains(foundId)) {
                            found.setFriendshipStatus("ACCEPTED");
                        } else if (incomingRequestUserIds.contains(foundId) || sentRequestUserIds.contains(foundId)) {
                            found.setFriendshipStatus("PENDING");
                        }
                    }

                    userList.clear();
                    userList.add(found);
                    userAdapter.notifyDataSetChanged();
                    showResults();
                } else {
                    showEmptyState("Không tìm thấy người dùng với số điện thoại này");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                showEmptyState("Lỗi kết nối. Vui lòng thử lại sau.");
            }
        });
    }

    private void showEmptyState(String message) {
        tvEmptyState.setText(message);
        tvEmptyState.setVisibility(View.VISIBLE);
        rvUsers.setVisibility(View.GONE);
    }

    private void showResults() {
        tvEmptyState.setVisibility(View.GONE);
        rvUsers.setVisibility(View.VISIBLE);
    }

    public void markAsSentRequest(int userId) {
        sentRequestUserIds.add(userId);
        incomingRequestUserIds.remove(userId);
        saveSentRequestsToPrefs();           // Lưu bền vững
        if (userAdapter != null) {
            userAdapter.notifyDataSetChanged();
        }
    }

    public void markAsFriend(int userId) {
        friendUserIds.add(userId);
        sentRequestUserIds.remove(userId);
        incomingRequestUserIds.remove(userId);
        saveSentRequestsToPrefs();

        for (User u : userList) {
            if (u != null && u.getId() != null && u.getId() == userId) {
                u.setFriendshipStatus("ACCEPTED");
            }
        }

        if (userAdapter != null) {
            userAdapter.notifyDataSetChanged();
        }
    }

    private void fetchMyFriends() {
        friendApi.getMyFriends(myUserId).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    friendUserIds.clear();
                    for (User u : response.body()) {
                        if (u != null && u.getId() != null) {
                            friendUserIds.add(u.getId());
                        }
                    }
                    if (userAdapter != null) userAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                // ignore
            }
        });
    }

    private void fetchIncomingRequests() {
        friendApi.getPendingRequests(myUserId).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    incomingRequestUserIds.clear();
                    for (User u : response.body()) {
                        if (u != null && u.getId() != null) {
                            incomingRequestUserIds.add(u.getId());
                        }
                    }
                    if (userAdapter != null) userAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                // ignore
            }
        });
    }

    private void fetchSentRequests() {
        friendApi.getSentRequests(myUserId).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Merge with local prefs; server is source-of-truth.
                    sentRequestUserIds.clear();
                    for (User u : response.body()) {
                        if (u != null && u.getId() != null) {
                            sentRequestUserIds.add(u.getId());
                        }
                    }
                    saveSentRequestsToPrefs();
                    if (userAdapter != null) userAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                // ignore
            }
        });
    }

    private void bindMyAvatar() {
        String myName = getSharedPreferences("ChatAppPrefs", MODE_PRIVATE).getString("myDisplayName", "Me");
        String avatar = getSharedPreferences("ChatAppPrefs", MODE_PRIVATE).getString("myAvatar", null);

        String url = (avatar == null || avatar.trim().isEmpty()) ?
                "https://ui-avatars.com/api/?name=" + URLEncoder.encode(myName, StandardCharsets.UTF_8) + "&size=128" :
                avatar;

        Glide.with(this)
                .load(url)
                .placeholder(R.drawable.avatar_placeholder)
                .error(R.drawable.avatar_placeholder)
                .circleCrop()
                .into(imgProfile);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_people) return true;
            else if (id == R.id.nav_chats) {
                startActivity(new Intent(this, ChatListActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            return false;
        });
        bottomNav.setSelectedItemId(R.id.nav_people);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Socket realtime: cập nhật nút thành "Bạn bè" ngay khi được accept.
        SocketManager socketManager = SocketManager.getInstance();
        socketManager.setMyUserId(myUserId);
        socketManager.connect();
        socketManager.setFriendshipListener((userIdA, userIdB) -> {
            if (myUserId == null || myUserId == -1) return;
            if (myUserId == userIdA) {
                runOnUiThread(() -> markAsFriend(userIdB));
            } else if (myUserId == userIdB) {
                runOnUiThread(() -> markAsFriend(userIdA));
            }
        });

        if (userAdapter != null) {
            userAdapter.notifyDataSetChanged();
        }
    }
}