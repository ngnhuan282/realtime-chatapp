package com.example.chatapp.view.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class EmojiPickerAdapter extends RecyclerView.Adapter<EmojiPickerAdapter.ViewHolder> {
    private List<String> emojis;
    private OnEmojiClickListener listener;

    public interface OnEmojiClickListener { void onEmojiClick(String emoji); }

    public EmojiPickerAdapter(List<String> emojis, OnEmojiClickListener listener) {
        this.emojis = emojis;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String emoji = emojis.get(position);
        holder.textView.setText(emoji);
        holder.textView.setTextSize(24);
        holder.textView.setGravity(android.view.Gravity.CENTER);
        holder.itemView.setOnClickListener(v -> listener.onEmojiClick(emoji));
    }

    @Override
    public int getItemCount() { return emojis.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ViewHolder(View v) { super(v); textView = v.findViewById(android.R.id.text1); }
    }
}