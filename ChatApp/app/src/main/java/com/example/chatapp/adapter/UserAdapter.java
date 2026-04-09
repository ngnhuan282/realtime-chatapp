package com.example.chatapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.model.User;
import com.example.chatapp.network.rest.ApiService; // Đã sửa đường dẫn theo đúng thư mục của bạn

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private Context context;
    private List<User> userList;
    private int currentUserId;
    private ApiService apiService;

    public UserAdapter(Context context, List<User> userList, int currentUserId, ApiService apiService) {
        this.context = context;
        this.userList = userList;
        this.currentUserId = currentUserId;
        this.apiService = apiService;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        // Hiển thị tên
        holder.txtUsername.setText(user.getDisplayName());

        // 1. HIỂN THỊ AVATAR (Sử dụng Glide)
        Glide.with(context)
                .load(user.getAvatar())
                .placeholder(R.mipmap.ic_launcher_round)
                .error(R.drawable.ic_launcher_background)
                .circleCrop()
                .into(holder.imgAvatar);

        // 2. XỬ LÝ NÚT KẾT BẠN
        holder.btnConnect.setOnClickListener(v -> {
            sendFriendRequest(user, holder.btnConnect);
        });
    }

    private void sendFriendRequest(User targetUser, Button btn) {
        // GỌI API GỬI LỜI MỜI (Sửa lỗi Callback ở đây)
        apiService.sendFriendRequest(currentUserId, targetUser.getId()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    btn.setText("Đã gửi");
                    btn.setEnabled(false);
                    Toast.makeText(context, "Đã gửi lời mời tới " + targetUser.getDisplayName(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Gửi thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Toast.makeText(context, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView txtUsername;
        Button btnConnect;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            txtUsername = itemView.findViewById(R.id.txtUsername);
            btnConnect = itemView.findViewById(R.id.btnConnect);
        }
    }
}
