package com.example.chatapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.model.User;
import com.google.android.material.imageview.ShapeableImageView;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class UserSelectAdapter extends RecyclerView.Adapter<UserSelectAdapter.UserSelectViewHolder> {

    private final Context context;
    private final List<User> userList;
    private final List<Integer> selectedUserIds = new ArrayList<>();

    public UserSelectAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserSelectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_select, parent, false);
        return new UserSelectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserSelectViewHolder holder, int position) {
        User user = userList.get(position);

        // Xử lý hiển thị tên an toàn (tránh null)
        String displayName = user.getDisplayName();
        if (displayName == null || displayName.trim().isEmpty()) {
            displayName = (user.getUsername() != null && !user.getUsername().isEmpty())
                    ? user.getUsername()
                    : "User " + (user.getId() != null ? user.getId() : "");
        }

        holder.tvDisplayName.setText(displayName);

        // Hiển thị username
        if (user.getUsername() != null && !user.getUsername().isEmpty()) {
            holder.tvUsername.setText("@" + user.getUsername());
            holder.tvUsername.setVisibility(View.VISIBLE);
        } else {
            holder.tvUsername.setVisibility(View.GONE);
        }

        // Load avatar
        String avatarUrl = user.getAvatar();
        if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
            try {
                avatarUrl = "https://ui-avatars.com/api/?name=" +
                        URLEncoder.encode(displayName, StandardCharsets.UTF_8) + "&size=128";
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

        // Xử lý checkbox
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(selectedUserIds.contains(user.getId()));

        holder.itemView.setOnClickListener(v -> {
            boolean isCurrentlySelected = selectedUserIds.contains(user.getId());

            if (isCurrentlySelected) {
                // Đang chọn -> Hủy chọn
                selectedUserIds.remove(Integer.valueOf(user.getId()));
                holder.checkBox.setChecked(false);
            } else {
                // Đang không chọn -> Chọn
                selectedUserIds.add(user.getId());
                holder.checkBox.setChecked(true);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public List<Integer> getSelectedUserIds() {
        return new ArrayList<>(selectedUserIds);
    }

    public void clearSelection() {
        selectedUserIds.clear();
        notifyDataSetChanged();
    }

    static class UserSelectViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView imgAvatar;
        TextView tvDisplayName;
        TextView tvUsername;
        CheckBox checkBox;

        public UserSelectViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvDisplayName = itemView.findViewById(R.id.tvDisplayName);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            checkBox = itemView.findViewById(R.id.checkbox);
        }
    }
}