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
import com.example.chatapp.network.rest.FriendApi;
import com.example.chatapp.view.people.PeopleActivity;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private final Context context;
    private final List<User> userList;
    private final int currentUserId;
    private final FriendApi friendApi;
    private final Set<Integer> sentRequestUserIds;   // Truyền từ PeopleActivity

    public UserAdapter(Context context, List<User> userList, int currentUserId,
                       FriendApi friendApi, Set<Integer> sentRequestUserIds) {
        this.context = context;
        this.userList = userList;
        this.currentUserId = currentUserId;
        this.friendApi = friendApi;
        this.sentRequestUserIds = sentRequestUserIds;
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

        holder.tvDisplayName.setText(user.getDisplayName());

        if (user.getUsername() != null && !user.getUsername().isEmpty()) {
            holder.tvUsername.setText("@" + user.getUsername());
            holder.tvUsername.setVisibility(View.VISIBLE);
        } else {
            holder.tvUsername.setVisibility(View.GONE);
        }

        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) {
            holder.tvPhoneNumber.setText(user.getPhoneNumber());
            holder.tvPhoneNumber.setVisibility(View.VISIBLE);
        } else {
            holder.tvPhoneNumber.setVisibility(View.GONE);
        }

        // Avatar
        String avatarUrl = user.getAvatar();
        if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
            try {
                avatarUrl = "https://ui-avatars.com/api/?name=" +
                        URLEncoder.encode(user.getDisplayName(), StandardCharsets.UTF_8) + "&size=128";
            } catch (Exception e) {
                avatarUrl = "https://ui-avatars.com/api/?name=User&size=128";
            }
        }

        Glide.with(context)
                .load(avatarUrl)
                .placeholder(R.drawable.avatar_placeholder)
                .error(R.drawable.avatar_placeholder)
                .circleCrop()
                .into(holder.imgAvatar);

        // Xử lý nút Kết bạn
        if (sentRequestUserIds.contains(user.getId())) {
            holder.btnConnect.setText("Đã gửi lời mời");
            holder.btnConnect.setEnabled(false);
        } else {
            holder.btnConnect.setText("Kết bạn");
            holder.btnConnect.setEnabled(true);
            holder.btnConnect.setOnClickListener(v -> sendFriendRequest(user, holder.btnConnect));
        }
    }

    private void sendFriendRequest(User targetUser, Button btn) {
        friendApi.sendFriendRequest(currentUserId, targetUser.getId())
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            btn.setText("Đã gửi lời mời");
                            btn.setEnabled(false);
                            Toast.makeText(context, "Đã gửi lời mời đến " + targetUser.getDisplayName(), Toast.LENGTH_SHORT).show();

                            // Cập nhật trạng thái trong PeopleActivity
                            if (context instanceof PeopleActivity) {
                                ((PeopleActivity) context).markAsSentRequest(targetUser.getId());
                            }
                        } else {
                            Toast.makeText(context, "Gửi lời mời thất bại", Toast.LENGTH_SHORT).show();
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
        TextView tvDisplayName, tvUsername, tvPhoneNumber;
        Button btnConnect;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvDisplayName = itemView.findViewById(R.id.tvDisplayName);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvPhoneNumber = itemView.findViewById(R.id.tvPhoneNumber);
            btnConnect = itemView.findViewById(R.id.btnConnect);
        }
    }
}