package com.example.chatapp.view.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.chatapp.R;
import com.example.chatapp.model.Conversation;
import com.google.android.material.imageview.ShapeableImageView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {
    private List<Conversation> conversationList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Conversation conversation);
    }

    public ChatListAdapter(List<Conversation> list, OnItemClickListener listener) {
        this.conversationList = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Conversation chat = conversationList.get(position);

        holder.tvName.setText(chat.getDisplayName());
        holder.tvLastMessage.setText(chat.getLastMessage());

        // Định dạng thời gian HH:mm (ví dụ 10:30 AM) [cite: 42]
        // Dùng hh:mm a để hiện AM/PM [cite: 42]
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        // Đảm bảo chat.getLastTime() trả về giá trị Long tính bằng miliseconds
        holder.tvTime.setText(sdf.format(new Date(chat.getLastTime())));

        // Xử lý hiển thị số tin nhắn chưa đọc (Badge)
        if (chat.getUnreadCount() > 0) {
            holder.unreadBadge.setVisibility(View.VISIBLE);
            holder.unreadBadge.setText(String.valueOf(chat.getUnreadCount()));
        } else {
            holder.unreadBadge.setVisibility(View.GONE);
        }

        // Sự kiện click chuyển sang màn hình chat chi tiết [cite: 36]
        holder.itemView.setOnClickListener(v -> listener.onItemClick(chat));
    }

    @Override
    public int getItemCount() { return conversationList.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvLastMessage, tvTime, unreadBadge;
        ShapeableImageView imgAvatar;

        ViewHolder(View v) {
            super(v);
            imgAvatar = v.findViewById(R.id.imgAvatar); // Khớp ID imgAvatar
            tvName = v.findViewById(R.id.tvName);
            tvLastMessage = v.findViewById(R.id.tvLastMessage);
            tvTime = v.findViewById(R.id.tvTime);
            unreadBadge = v.findViewById(R.id.unreadBadge); // Khớp ID unreadBadge
        }
    }
}