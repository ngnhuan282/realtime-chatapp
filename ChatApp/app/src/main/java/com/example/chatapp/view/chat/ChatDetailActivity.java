package com.example.chatapp.view.chat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Bundle;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.emoji2.emojipicker.EmojiPickerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.R;
import com.example.chatapp.adapter.ChatAdapter;
import com.example.chatapp.model.Message;
import com.example.chatapp.network.rest.ApiClient;
import com.example.chatapp.network.rest.MessageApi;
import com.example.chatapp.network.socket.SocketManager;
import com.example.chatapp.view.darkmode.BaseActivity;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatDetailActivity extends BaseActivity {

    private ChatAdapter adapter;
    private List<Message> messages = new ArrayList<>();
    private SocketManager socketManager;
    private EditText edtMessage;
    private RecyclerView rvMessages;
    private EmojiPickerView emojiPicker;
    private FloatingActionButton btnSend;
    private ImageView btnBack, btnAttach, imgAvatar, btnEmoji;
    private TextView tvFriendName, tvConnectionBanner;

    private Integer myUserId;
    private Integer friendId;
    private String friendName;

    // Biến hỗ trợ Chat Nhóm
    private Integer groupId = -1;
    private boolean isGroupChat = false;
    private String groupName;

    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private boolean isInternetAvailable = true;

    private final Gson gson = new Gson();
    private final Runnable hideBannerRunnable = this::hideConnectionBanner;

    private SharedPreferences cachePrefs;

    // Launchers
    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    Log.d("Picker", "Đã chọn URI: " + uri);
                    uploadFile(uri, "IMAGE");
                }
            });

    private final ActivityResultLauncher<String> videoPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    Log.d("Picker", "Đã chọn URI video: " + uri);
                    uploadFile(uri, "VIDEO");
                }
            });

    private final ActivityResultLauncher<String> locationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    shareCurrentLocationInternal();
                } else {
                    Toast.makeText(this, "Bạn cần cấp quyền vị trí để gửi location", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        SharedPreferences sharedPref = getSharedPreferences("ChatAppPrefs", Context.MODE_PRIVATE);
        myUserId = sharedPref.getInt("myUserId", -1);

        friendId = getIntent().getIntExtra("friendId", -1);
        friendName = getIntent().getStringExtra("friendName");
        groupId = getIntent().getIntExtra("groupId", -1);
        groupName = getIntent().getStringExtra("groupName");
        isGroupChat = groupId != -1;

        initViews();
        setupRecyclerView();
        setupEmojiPicker();
        cachePrefs = getSharedPreferences("ChatCachePrefs", MODE_PRIVATE);
        setupNetworkMonitoring();

        socketManager = SocketManager.getInstance();
        socketManager.setMyUserId(myUserId);
        socketManager.setListener(msg -> runOnUiThread(() -> handleNewMessage(msg)));
        socketManager.setConnectionListener(connected -> runOnUiThread(() -> {
            if (connected) {
                showConnectionBanner("Đang kết nối...", false, true);
            } else {
                showConnectionBanner("Mất kết nối Internet", true, false);
            }
        }));
        socketManager.setMessageStatusListener((localId, status) -> runOnUiThread(() -> updateLocalMessageStatus(localId, status)));
        socketManager.connect();

        loadCachedMessages();

        if (!isInternetAvailable) {
            showConnectionBanner("Mất kết nối Internet", true, false);
        } else {
            hideConnectionBanner();
            if (isGroupChat) {
                tvFriendName.setText(groupName != null ? groupName : "Nhóm chat");
                loadGroupHistory();
            } else {
                if (friendName != null) tvFriendName.setText(friendName);
                loadHistory();
            }
        }

        btnSend.setOnClickListener(v -> sendTextMessage());
        btnAttach.setOnClickListener(v -> showAttachmentPicker());
        btnEmoji.setOnClickListener(v -> toggleEmojiPicker());
        edtMessage.setOnClickListener(v -> hideEmojiPickerIfVisible());
        btnBack.setOnClickListener(v -> finish());
    }

    private void initViews() {
        rvMessages = findViewById(R.id.rvMessages);
        emojiPicker = findViewById(R.id.emojiPicker);
        edtMessage = findViewById(R.id.edtMessage);
        btnSend = findViewById(R.id.btnSend);
        btnBack = findViewById(R.id.btnBack);
        btnAttach = findViewById(R.id.btnAttach);
        btnEmoji = findViewById(R.id.btnEmoji);
        imgAvatar = findViewById(R.id.imgAvatar);
        tvFriendName = findViewById(R.id.tvFriendName);
        tvConnectionBanner = findViewById(R.id.tvConnectionBanner);
    }

    private void setupRecyclerView() {
        adapter = new ChatAdapter(messages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(adapter);
    }

    private void setupEmojiPicker() {
        emojiPicker.setOnEmojiPickedListener(emojiViewItem -> {
            String emoji = emojiViewItem.getEmoji();
            int start = Math.max(edtMessage.getSelectionStart(), 0);
            int end = Math.max(edtMessage.getSelectionEnd(), 0);
            edtMessage.getText().replace(Math.min(start, end), Math.max(start, end), emoji);
        });
    }

    private void sendTextMessage() {
        String text = edtMessage.getText().toString().trim();
        if (!text.isEmpty()) {
            sendSocketMessage(text, "TEXT");
            edtMessage.setText("");
            emojiPicker.setVisibility(View.GONE);
        }
    }

    private void sendSocketMessage(String content, String type) {
        Message newMsg = new Message();
        newMsg.setLocalId(UUID.randomUUID().toString());
        newMsg.setSenderId(myUserId);
        newMsg.setContent(content);
        newMsg.setTimestamp(System.currentTimeMillis());
        newMsg.setMessageType(type);
        newMsg.setMe(true);
        newMsg.setStatus(Message.STATUS_SENDING);

        if (isGroupChat) {
            newMsg.setGroupId(groupId);
            newMsg.setReceiverId(null);           // ← ĐÃ FIX: null cho tin nhắn nhóm
        } else {
            newMsg.setReceiverId(friendId);
            newMsg.setGroupId(null);
        }

        messages.add(newMsg);
        adapter.notifyItemInserted(messages.size() - 1);
        rvMessages.scrollToPosition(messages.size() - 1);
        saveMessagesToCache();

        if (!hasInternetConnection()) {
            showConnectionBanner("Mất kết nối Internet", true, false);
        }

        try {
            socketManager.sendMessage(newMsg);
        } catch (Exception e) {
            Log.e("ChatDetail", "Send message error: " + e.getMessage());
        }
    }

    private void handleNewMessage(Message msg) {
        boolean belongToThisChat = false;
        if (isGroupChat) {
            belongToThisChat = msg.getGroupId() != null && msg.getGroupId().equals(groupId);
        } else {
            belongToThisChat = msg.getGroupId() == null &&
                    ((msg.getSenderId() != null && msg.getSenderId().equals(friendId)) ||
                            (msg.getReceiverId() != null && msg.getReceiverId().equals(friendId)));
        }

        if (belongToThisChat) {
            msg.setMe(msg.getSenderId() != null && msg.getSenderId().equals(myUserId));
            messages.add(msg);
            adapter.notifyItemInserted(messages.size() - 1);
            rvMessages.scrollToPosition(messages.size() - 1);
            saveMessagesToCache();
        }
    }

    private void toggleEmojiPicker() {
        if (emojiPicker.getVisibility() == View.GONE) {
            hideKeyboard();
            emojiPicker.setVisibility(View.VISIBLE);
        } else {
            emojiPicker.setVisibility(View.GONE);
        }
    }

    private void hideEmojiPickerIfVisible() {
        if (emojiPicker.getVisibility() == View.VISIBLE) {
            emojiPicker.setVisibility(View.GONE);
        }
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    private void showAttachmentPicker() {
        new AlertDialog.Builder(this)
                .setTitle("Chọn loại tệp")
                .setItems(new CharSequence[]{"Hình ảnh", "Video", "Vị trí"}, (dialog, which) -> {
                    if (which == 0) imagePickerLauncher.launch("image/*");
                    else if (which == 1) videoPickerLauncher.launch("video/*");
                    else shareCurrentLocation();
                })
                .show();
    }

    private void shareCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            shareCurrentLocationInternal();
            return;
        }
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void shareCurrentLocationInternal() {
        try {
            Toast.makeText(this, "Đang lấy vị trí hiện tại...", Toast.LENGTH_SHORT).show();
            CancellationTokenSource cts = new CancellationTokenSource();
            LocationServices.getFusedLocationProviderClient(this)
                    .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.getToken())
                    .addOnSuccessListener(location -> {
                        if (location == null) {
                            Toast.makeText(this, "Không lấy được vị trí hiện tại, hãy bật GPS rồi thử lại", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String mapUrl = String.format(Locale.US, "https://maps.google.com/?q=%f,%f", location.getLatitude(), location.getLongitude());
                        sendSocketMessage(mapUrl, "LOCATION");
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi lấy vị trí: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } catch (SecurityException e) {
            Toast.makeText(this, "Thiếu quyền vị trí", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadFile(Uri uri, String messageType) {
        if (!hasInternetConnection()) {
            showConnectionBanner("Mất kết nối Internet", true, false);
            Toast.makeText(this, "Mất kết nối Internet", Toast.LENGTH_SHORT).show();
            return;
        }
        File file = uriToFile(uri, messageType);
        if (file == null) {
            Toast.makeText(this, "Lỗi truy cập tệp tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        String mediaType = "VIDEO".equals(messageType) ? "video/*" : "image/*";
        RequestBody requestFile = RequestBody.create(file, MediaType.parse(mediaType));
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        MessageApi messageApi = ApiClient.getClient().create(MessageApi.class);
        Call<ResponseBody> request = "VIDEO".equals(messageType) ? messageApi.uploadVideo(body) : messageApi.uploadFile(body);

        request.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String fileName = response.body().string();
                        sendSocketMessage(fileName, messageType);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(ChatDetailActivity.this, "Lỗi kết nối Server!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private File uriToFile(Uri uri, String messageType) {
        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            if (inputStream == null) return null;
            String extension = getFileExtension(uri, messageType);
            File tempFile = new File(getCacheDir(), "upload_" + System.currentTimeMillis() + extension);
            try (FileOutputStream out = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[8192];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
                out.flush();
            }
            return tempFile;
        } catch (Exception e) {
            Log.e("uriToFile", "Error: " + e.getMessage());
            return null;
        }
    }

    private String getFileExtension(Uri uri, String messageType) {
        String mimeType = getContentResolver().getType(uri);
        if (mimeType != null && mimeType.contains("/")) {
            String ext = mimeType.substring(mimeType.indexOf('/') + 1);
            if (!ext.isBlank()) return "." + ext;
        }
        return "VIDEO".equals(messageType) ? ".mp4" : ".jpg";
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
                        m.setStatus(Message.STATUS_SENT);
                        messages.add(m);
                    }
                    adapter.notifyDataSetChanged();
                    if (!messages.isEmpty()) rvMessages.scrollToPosition(messages.size() - 1);
                    saveMessagesToCache();
                }
            }
            @Override
            public void onFailure(Call<List<Message>> call, Throwable t) {
                Log.e("ChatDetail", "Load history failed: " + t.getMessage());
                loadCachedMessages();
            }
        });
    }

    private void loadGroupHistory() {
        if (groupId == -1) return;
        MessageApi api = ApiClient.getClient().create(MessageApi.class);
        api.getGroupHistory(groupId).enqueue(new Callback<List<Message>>() {
            @Override
            public void onResponse(Call<List<Message>> call, Response<List<Message>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    messages.clear();
                    for (Message m : response.body()) {
                        m.setMe(m.getSenderId().equals(myUserId));
                        m.setStatus(Message.STATUS_SENT);
                        messages.add(m);
                    }
                    adapter.notifyDataSetChanged();
                    if (!messages.isEmpty()) rvMessages.scrollToPosition(messages.size() - 1);
                    saveMessagesToCache();
                }
            }
            @Override
            public void onFailure(Call<List<Message>> call, Throwable t) {
                Log.e("ChatDetail", "Load group history failed: " + t.getMessage());
                loadCachedMessages();
            }
        });
    }

    private void setupNetworkMonitoring() {
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        isInternetAvailable = hasInternetConnection();
        if (connectivityManager == null) return;

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override public void onAvailable(Network network) {
                isInternetAvailable = true;
                runOnUiThread(() -> showConnectionBanner("Đang kết nối...", false, true));
                if (socketManager != null) socketManager.connect();
            }
            @Override public void onLost(Network network) {
                isInternetAvailable = false;
                runOnUiThread(() -> showConnectionBanner("Mất kết nối Internet", true, false));
            }
        };

        NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();
        connectivityManager.registerNetworkCallback(request, networkCallback);
    }

    private boolean hasInternetConnection() {
        if (connectivityManager == null) return false;
        Network activeNetwork = connectivityManager.getActiveNetwork();
        if (activeNetwork == null) return false;
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
        return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    private String getCacheKey() {
        if (isGroupChat) return "group_" + groupId;
        return "dm_" + myUserId + "_" + friendId;
    }

    private void saveMessagesToCache() {
        String json = gson.toJson(messages);
        cachePrefs.edit().putString(getCacheKey(), json).apply();
    }

    private void loadCachedMessages() {
        String raw = cachePrefs.getString(getCacheKey(), null);
        if (raw == null || raw.trim().isEmpty()) return;

        Type listType = new TypeToken<List<Message>>() {}.getType();
        List<Message> cached = gson.fromJson(raw, listType);
        if (cached == null || cached.isEmpty()) return;

        messages.clear();
        for (Message m : cached) {
            m.setMe(m.getSenderId() != null && m.getSenderId().equals(myUserId));
            if (m.getLocalId() == null) m.setStatus(Message.STATUS_SENT);
            messages.add(m);
        }
        adapter.notifyDataSetChanged();
        rvMessages.scrollToPosition(messages.size() - 1);
    }

    private void updateLocalMessageStatus(String localId, String status) {
        if (localId == null) return;
        for (int i = messages.size() - 1; i >= 0; i--) {
            Message msg = messages.get(i);
            if (localId.equals(msg.getLocalId())) {
                msg.setStatus(status);
                adapter.notifyItemChanged(i);
                saveMessagesToCache();
                return;
            }
        }
    }

    private void showConnectionBanner(String text, boolean isError, boolean autoHide) {
        if (tvConnectionBanner == null) return;
        tvConnectionBanner.setText(text);
        tvConnectionBanner.setBackgroundColor(isError ? Color.parseColor("#D32F2F") : Color.parseColor("#2E7D32"));
        tvConnectionBanner.setVisibility(View.VISIBLE);
        if (autoHide) {
            tvConnectionBanner.removeCallbacks(hideBannerRunnable);
            tvConnectionBanner.postDelayed(hideBannerRunnable, 1500);
        }
    }

    private void hideConnectionBanner() {
        if (tvConnectionBanner != null) tvConnectionBanner.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (connectivityManager != null && networkCallback != null) {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback);
            } catch (Exception e) {
                Log.w("ChatDetail", "Cannot unregister network callback: " + e.getMessage());
            }
        }
        socketManager.setConnectionListener(null);
        socketManager.setMessageStatusListener(null);
    }
}