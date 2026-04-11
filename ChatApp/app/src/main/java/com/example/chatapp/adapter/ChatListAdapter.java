package com.example.chatapp.adapter;

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

    private final List<Conversation> conversationList;
    private final OnConversationClickListener listener;

    public ChatListAdapter(List<Conversation> list, OnConversationClickListener listener) {
        this.conversationList = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Conversation chat = conversationList.get(position);

        holder.tvName.setText(chat.getDisplayName());
        holder.tvLastMessage.setText(chat.getLastMessage());

        if (chat.getLastTime() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            holder.tvTime.setText(sdf.format(new Date(chat.getLastTime())));
        }

        if (chat.getUnreadCount() > 0) {
            holder.unreadBadge.setVisibility(View.VISIBLE);
            holder.unreadBadge.setText(String.valueOf(chat.getUnreadCount()));
        } else {
            holder.unreadBadge.setVisibility(View.GONE);
        }

        if (chat.isGroup()) {
            holder.imgAvatar.setImageResource(R.drawable.ic_chatgroup);
        } else {
            holder.imgAvatar.setImageResource(R.drawable.sample_avatar);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(chat);
            }
        });
    }

    @Override
    public int getItemCount() {
        return conversationList.size();
    }

    // ViewHolder (static nested class)
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvLastMessage, tvTime, unreadBadge;
        ShapeableImageView imgAvatar;

        ViewHolder(View v) {
            super(v);
            imgAvatar = v.findViewById(R.id.imgAvatar);
            tvName = v.findViewById(R.id.tvName);
            tvLastMessage = v.findViewById(R.id.tvLastMessage);
            tvTime = v.findViewById(R.id.tvTime);
            unreadBadge = v.findViewById(R.id.unreadBadge);
        }
    }
}