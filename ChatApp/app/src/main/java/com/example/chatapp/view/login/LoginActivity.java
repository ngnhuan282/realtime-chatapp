package com.example.chatapp.view.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chatapp.R;
import com.example.chatapp.network.rest.ApiClient;
import com.example.chatapp.network.rest.AuthApi;
import com.example.chatapp.model.LoginRequest;
import com.example.chatapp.model.LoginResponse;
import com.example.chatapp.view.chat.ChatListActivity;
import com.example.chatapp.view.darkmode.BaseActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends BaseActivity {
    private EditText edtUsername, edtPassword;
    private Button btnSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnSignIn = findViewById(R.id.btnLogin);

        btnSignIn.setOnClickListener(v -> handleLogin());
    }

    private void handleLogin() {
        String user = edtUsername.getText().toString().trim();
        String pass = edtPassword.getText().toString().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        LoginRequest request = new LoginRequest(user, pass);
        AuthApi authApi = ApiClient.getClient().create(AuthApi.class);

        authApi.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginData = response.body();

                    // LƯU Ý: Lưu userId vào SharedPreferences để dùng cho việc gửi tin nhắn sau này [cite: 32]
                    SharedPreferences sharedPref = getSharedPreferences("ChatAppPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putInt("myUserId", loginData.getId()); // Giả sử LoginResponse có getId() trả về Integer
                    editor.putString("myDisplayName", loginData.getDisplayName());
                    editor.putString("myAvatar", loginData.getAvatar());
                    editor.apply();

                    String displayName = loginData.getDisplayName();
                    Toast.makeText(LoginActivity.this,
                            "Chào " + (displayName != null ? displayName : loginData.getUsername()),
                            Toast.LENGTH_SHORT).show();

                    // Chuyển sang màn hình danh sách chat
                    Intent intent = new Intent(LoginActivity.this, ChatListActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}