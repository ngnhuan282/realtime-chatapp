package com.example.chatapp.view.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.model.Message;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_SENT_TEXT = 1;
    private static final int TYPE_RECEIVED_TEXT = 2;
    private static final int TYPE_SENT_IMAGE = 3;
    private static final int TYPE_RECEIVED_IMAGE = 4;

    private List<Message> messageList;

    public ChatAdapter(List<Message> messageList) { this.messageList = messageList; }

    @Override
    public int getItemViewType(int position) {
        Message msg = messageList.get(position);
        if (msg.isMe()) {
            return "IMAGE".equals(msg.getMessageType()) ? TYPE_SENT_IMAGE : TYPE_SENT_TEXT;
        } else {
            return "IMAGE".equals(msg.getMessageType()) ? TYPE_RECEIVED_IMAGE : TYPE_RECEIVED_TEXT;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutRes;
        switch (viewType) {
            case TYPE_SENT_IMAGE: layoutRes = R.layout.item_chat_image_sent; break;
            case TYPE_RECEIVED_IMAGE: layoutRes = R.layout.item_chat_image_received; break;
            case TYPE_RECEIVED_TEXT: layoutRes = R.layout.item_chat_received; break;
            default: layoutRes = R.layout.item_chat_sent; break;
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((MessageViewHolder) holder).bind(messageList.get(position));
    }

    @Override
    public int getItemCount() { return messageList.size(); }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView txtMessage, txtTime;
        ImageView imgContent;

        MessageViewHolder(View itemView) {
            super(itemView);
            txtMessage = itemView.findViewById(R.id.txt_message_content);
            txtTime = itemView.findViewById(R.id.txt_message_time);
            imgContent = itemView.findViewById(R.id.img_message_content);
        }

        void bind(Message message) {
            if ("IMAGE".equals(message.getMessageType())) {
                if (txtMessage != null) txtMessage.setVisibility(View.GONE);
                if (imgContent != null) {
                    imgContent.setVisibility(View.VISIBLE);
                    // Server URL (Thay IP thật nếu dùng máy thật)
                    String imageUrl = "http://10.0.2.2:8080/uploads/" + message.getContent();
                    Glide.with(itemView.getContext()).load(imageUrl).into(imgContent);
                }
            } else {
                if (imgContent != null) imgContent.setVisibility(View.GONE);
                if (txtMessage != null) {
                    txtMessage.setVisibility(View.VISIBLE);
                    txtMessage.setText(message.getContent());
                }
            }

            if (txtTime != null && message.getTimestamp() != 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                txtTime.setText(sdf.format(new Date(message.getTimestamp())));
            }
        }
    }
}