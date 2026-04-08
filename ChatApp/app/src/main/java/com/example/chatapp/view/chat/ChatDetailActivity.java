package com.example.chatapp.view.chat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.emoji2.emojipicker.EmojiPickerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.R;
import com.example.chatapp.model.Message;
import com.example.chatapp.network.rest.ApiClient;
import com.example.chatapp.network.rest.MessageApi;
import com.example.chatapp.network.socket.SocketManager;
import com.example.chatapp.view.darkmode.BaseActivity;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
    private EmojiPickerView emojiPicker; // Khai báo EmojiPickerView từ thư viện
    private FloatingActionButton btnSend;
    private ImageView btnBack, btnAttach, imgAvatar, btnEmoji;
    private TextView tvFriendName;

    private Integer myUserId;
    private Integer friendId;
    private String friendName;

    // Launcher để chọn ảnh từ thư viện
    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    Log.d("Picker", "Đã chọn URI: " + uri);
                    uploadFile(uri, "IMAGE");
                }
            }
    );

    private final ActivityResultLauncher<String> videoPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    Log.d("Picker", "Đã chọn URI video: " + uri);
                    uploadFile(uri, "VIDEO");
                }
            }
    );

    private final ActivityResultLauncher<String> locationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    shareCurrentLocationInternal();
                } else {
                    Toast.makeText(this, "Bạn cần cấp quyền vị trí để gửi location", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        // Nhận thông tin người dùng và bạn bè
        SharedPreferences sharedPref = getSharedPreferences("ChatAppPrefs", Context.MODE_PRIVATE);
        myUserId = sharedPref.getInt("myUserId", -1);
        friendId = getIntent().getIntExtra("friendId", -1);
        friendName = getIntent().getStringExtra("friendName");

        initViews();
        setupRecyclerView();
        setupEmojiPicker();

        // Kết nối Socket
        socketManager = SocketManager.getInstance();
        socketManager.setMyUserId(myUserId);
        socketManager.setListener(msg -> {
            runOnUiThread(() -> {
                if (msg.getSenderId() != null &&
                        (msg.getSenderId().equals(friendId) || msg.getSenderId().equals(myUserId))) {
                    msg.setMe(msg.getSenderId().equals(myUserId));
                    messages.add(msg);
                    adapter.notifyItemInserted(messages.size() - 1);
                    rvMessages.scrollToPosition(messages.size() - 1);
                }
            });
        });
        socketManager.connect();

        loadHistory();

        // Xử lý gửi tin nhắn văn bản
        btnSend.setOnClickListener(v -> {
            String text = edtMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                sendSocketMessage(text, "TEXT");
                edtMessage.setText("");
                emojiPicker.setVisibility(View.GONE); // Ẩn bộ chọn sau khi gửi
            }
        });

        // Xử lý chọn tệp đính kèm
        btnAttach.setOnClickListener(v -> showAttachmentPicker());

        // Xử lý nhấn nút Emoji
        btnEmoji.setOnClickListener(v -> {
            if (emojiPicker.getVisibility() == View.GONE) {
                hideKeyboard(); // Ẩn bàn phím trước khi hiện Emoji
                emojiPicker.setVisibility(View.VISIBLE);
            } else {
                emojiPicker.setVisibility(View.GONE);
            }
        });

        // Ẩn bảng Emoji khi người dùng chạm vào ô nhập liệu để gõ chữ
        edtMessage.setOnClickListener(v -> {
            if (emojiPicker.getVisibility() == View.VISIBLE) {
                emojiPicker.setVisibility(View.GONE);
            }
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private void initViews() {
        rvMessages = findViewById(R.id.rvMessages);
        emojiPicker = findViewById(R.id.emojiPicker); // Ánh xạ từ XML
        edtMessage = findViewById(R.id.edtMessage);
        btnSend = findViewById(R.id.btnSend);
        btnBack = findViewById(R.id.btnBack);
        btnAttach = findViewById(R.id.btnAttach);
        btnEmoji = findViewById(R.id.btnEmoji);
        imgAvatar = findViewById(R.id.imgAvatar);
        tvFriendName = findViewById(R.id.tvFriendName);
        if (friendName != null) tvFriendName.setText(friendName);
    }

    private void setupEmojiPicker() {
        // Thiết lập sự kiện chọn Emoji từ thư viện androidx.emoji2
        emojiPicker.setOnEmojiPickedListener(emojiViewItem -> {
            String emoji = emojiViewItem.getEmoji();
            int start = Math.max(edtMessage.getSelectionStart(), 0);
            int end = Math.max(edtMessage.getSelectionEnd(), 0);
            // Chèn Emoji vào đúng vị trí con trỏ trong EditText
            edtMessage.getText().replace(Math.min(start, end), Math.max(start, end), emoji);
        });
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
                    if (which == 0) {
                        imagePickerLauncher.launch("image/*");
                    } else if (which == 1) {
                        videoPickerLauncher.launch("video/*");
                    } else {
                        shareCurrentLocation();
                    }
                })
                .show();
    }

    private void shareCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
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

                        String mapUrl = String.format(
                                Locale.US,
                                "https://maps.google.com/?q=%f,%f",
                                location.getLatitude(),
                                location.getLongitude()
                        );
                        sendSocketMessage(mapUrl, "LOCATION");
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Lỗi lấy vị trí: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        } catch (SecurityException e) {
            Toast.makeText(this, "Thiếu quyền vị trí", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadFile(Uri uri, String messageType) {
        File file = uriToFile(uri, messageType);
        if (file == null) {
            Toast.makeText(this, "Lỗi truy cập tệp tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        String mediaType = "VIDEO".equals(messageType) ? "video/*" : "image/*";
        RequestBody requestFile = RequestBody.create(MediaType.parse(mediaType), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        MessageApi messageApi = ApiClient.getClient().create(MessageApi.class);
        Call<ResponseBody> request = "VIDEO".equals(messageType)
                ? messageApi.uploadVideo(body)
                : messageApi.uploadFile(body);

        request.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String fileName = response.body().string();
                        sendSocketMessage(fileName, messageType);
                    } catch (Exception e) { e.printStackTrace(); }
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
            if (!ext.isBlank()) {
                return "." + ext;
            }
        }
        return "VIDEO".equals(messageType) ? ".mp4" : ".jpg";
    }

    private void sendSocketMessage(String content, String type) {
        Message newMsg = new Message(myUserId, friendId, content, System.currentTimeMillis(), true);
        newMsg.setMessageType(type);
        messages.add(newMsg);
        adapter.notifyItemInserted(messages.size() - 1);
        rvMessages.scrollToPosition(messages.size() - 1);
        socketManager.sendMessage(newMsg);
    }

    private void setupRecyclerView() {
        adapter = new ChatAdapter(messages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
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
                    if (!messages.isEmpty()) rvMessages.scrollToPosition(messages.size() - 1);
                }
            }
            @Override public void onFailure(Call<List<Message>> call, Throwable t) {}
        });
    }
}