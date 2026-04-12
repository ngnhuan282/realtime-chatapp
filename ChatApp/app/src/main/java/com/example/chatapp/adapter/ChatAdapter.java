package com.example.chatapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.model.Message;
import com.example.chatapp.view.chat.VideoPlayerActivity;
import com.google.android.material.imageview.ShapeableImageView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_SENT_TEXT = 1;
    private static final int TYPE_RECEIVED_TEXT = 2;
    private static final int TYPE_SENT_IMAGE = 3;
    private static final int TYPE_RECEIVED_IMAGE = 4;
    private static final int TYPE_SENT_VIDEO = 5;
    private static final int TYPE_RECEIVED_VIDEO = 6;
    private static final int TYPE_SENT_LOCATION = 7;
    private static final int TYPE_RECEIVED_LOCATION = 8;
    private static final String DEFAULT_LOCATION_DISPLAY = "Vị trí đã chia sẻ";
    private static final String LOCATION_LOCK_PREF = "location_lock_pref";
    private static final String LOCATION_LOCK_KEY_PREFIX = "location_lock_";

    private List<Message> messageList;

    public ChatAdapter(List<Message> messageList) { this.messageList = messageList; }

    private String buildLocationLockKey(Message message) {
        String sender = message.getSenderId() == null ? "null" : String.valueOf(message.getSenderId());
        String receiver = message.getReceiverId() == null ? "null" : String.valueOf(message.getReceiverId());
        String content = message.getContent() == null ? "" : message.getContent();
        return LOCATION_LOCK_KEY_PREFIX + sender + "_" + receiver + "_" + message.getTimestamp() + "_" + content.hashCode();
    }

    private boolean isLocationLockedPersisted(Context context, Message message) {
        SharedPreferences prefs = context.getSharedPreferences(LOCATION_LOCK_PREF, Context.MODE_PRIVATE);
        return prefs.getBoolean(buildLocationLockKey(message), false);
    }

    private void persistLocationLocked(Context context, Message message) {
        SharedPreferences prefs = context.getSharedPreferences(LOCATION_LOCK_PREF, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(buildLocationLockKey(message), true).apply();
    }

    @Override
    public int getItemViewType(int position) {
        Message msg = messageList.get(position);
        if (msg.isMe()) {
            if ("IMAGE".equals(msg.getMessageType())) return TYPE_SENT_IMAGE;
            if ("VIDEO".equals(msg.getMessageType())) return TYPE_SENT_VIDEO;
            if ("LOCATION".equals(msg.getMessageType())) return TYPE_SENT_LOCATION;
            return TYPE_SENT_TEXT;
        } else {
            if ("IMAGE".equals(msg.getMessageType())) return TYPE_RECEIVED_IMAGE;
            if ("VIDEO".equals(msg.getMessageType())) return TYPE_RECEIVED_VIDEO;
            if ("LOCATION".equals(msg.getMessageType())) return TYPE_RECEIVED_LOCATION;
            return TYPE_RECEIVED_TEXT;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutRes;
        switch (viewType) {
            case TYPE_SENT_IMAGE: layoutRes = R.layout.item_chat_image_sent; break;
            case TYPE_RECEIVED_IMAGE: layoutRes = R.layout.item_chat_image_received; break;
            case TYPE_SENT_VIDEO: layoutRes = R.layout.item_chat_image_sent; break;
            case TYPE_RECEIVED_VIDEO: layoutRes = R.layout.item_chat_image_received; break;
            case TYPE_SENT_LOCATION: layoutRes = R.layout.item_chat_location_sent; break;
            case TYPE_RECEIVED_LOCATION: layoutRes = R.layout.item_chat_location_received; break;
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

    class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView txtMessage, txtTime, txtSenderName;
        ImageView imgContent, imgStatusPending, imgStatusSending;
        Button btnStopLocation;
        ShapeableImageView imgAvatarReceived;
        Animation sendingRotateAnimation;

        MessageViewHolder(View itemView) {
            super(itemView);
            txtMessage = itemView.findViewById(R.id.txt_message_content);
            txtTime = itemView.findViewById(R.id.txt_message_time);
            if (txtTime == null) {
                txtTime = itemView.findViewById(R.id.txt_time);
            }
            imgContent = itemView.findViewById(R.id.img_message_content);
            imgStatusPending = itemView.findViewById(R.id.img_status_pending);
            imgStatusSending = itemView.findViewById(R.id.img_status_sending);
            btnStopLocation = itemView.findViewById(R.id.btn_stop_location);
            txtSenderName = itemView.findViewById(R.id.txt_sender_name);
            imgAvatarReceived = itemView.findViewById(R.id.img_avatar_received);
            sendingRotateAnimation = AnimationUtils.loadAnimation(itemView.getContext(), R.anim.sending_rotate);
        }

        void bind(Message message) {
            if (message.getGroupId() != null && message.getGroupId() > 0 && !message.isMe()) {
                if (txtSenderName != null) {
                    txtSenderName.setVisibility(View.VISIBLE);
                    txtSenderName.setText(message.getSenderName() != null ? message.getSenderName() : "Người dùng");
                }
                
                if (imgAvatarReceived != null) {
                    String avatarUrl = message.getSenderAvatar();
                    if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
                        avatarUrl = "https://ui-avatars.com/api/?name=" + 
                            (message.getSenderName() != null ? message.getSenderName() : "U") + "&size=128";
                    }
                    Glide.with(itemView.getContext())
                            .load(avatarUrl)
                            .placeholder(R.drawable.sample_avatar) // Cập nhật hình mặc định nếu cần
                            .circleCrop()
                            .into(imgAvatarReceived);
                }
            } else {
                if (txtSenderName != null) {
                    txtSenderName.setVisibility(View.GONE);
                }
            }
            if ("IMAGE".equals(message.getMessageType())) {
                if (txtMessage != null) txtMessage.setVisibility(View.GONE);
                if (imgContent != null) {
                    imgContent.setVisibility(View.VISIBLE);
                    // Server URL (Thay IP thật nếu dùng máy thật)
                    String imageUrl = "http://10.0.2.2:8080/uploads/" + message.getContent();
                    Glide.with(itemView.getContext()).load(imageUrl).into(imgContent);
                    imgContent.setOnClickListener(null);
                }
            } else if ("VIDEO".equals(message.getMessageType())) {
                if (txtMessage != null) txtMessage.setVisibility(View.GONE);
                if (imgContent != null) {
                    imgContent.setVisibility(View.VISIBLE);
                    String videoPath = message.getContent() == null ? "" : message.getContent();
                    if (!videoPath.startsWith("video/")) {
                        videoPath = "video/" + videoPath;
                    }
                        String videoUrl = "http://10.0.2.2:8080/uploads/" + videoPath;
                    Glide.with(itemView.getContext())
                            .load(videoUrl)
                            .placeholder(R.drawable.ic_video)
                            .error(R.drawable.ic_video)
                            .into(imgContent);

                    imgContent.setOnClickListener(v -> {
                        Intent intent = new Intent(itemView.getContext(), VideoPlayerActivity.class);
                        intent.putExtra(VideoPlayerActivity.EXTRA_VIDEO_URL, videoUrl);
                        itemView.getContext().startActivity(intent);
                    });
                }
                if (btnStopLocation != null) {
                    btnStopLocation.setVisibility(View.GONE);
                }
            } else if ("LOCATION".equals(message.getMessageType())) {
                if (imgContent != null) {
                    imgContent.setVisibility(View.GONE);
                    imgContent.setOnClickListener(null);
                }

                boolean isLocked = message.isLocationLocked() || isLocationLockedPersisted(itemView.getContext(), message);
                message.setLocationLocked(isLocked);

                if (txtMessage != null) {
                    txtMessage.setVisibility(View.VISIBLE);
                    if (isLocked) {
                        txtMessage.setText(DEFAULT_LOCATION_DISPLAY + "");
                        txtMessage.setOnClickListener(null);
                    } else {
                        txtMessage.setText(DEFAULT_LOCATION_DISPLAY + " (nhấn để mở bản đồ)");
                        txtMessage.setOnClickListener(v -> {
                            try {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(message.getContent()));
                                itemView.getContext().startActivity(intent);
                            } catch (Exception e) {
                                Toast.makeText(itemView.getContext(), "Không mở được bản đồ", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                if (btnStopLocation != null) {
                    btnStopLocation.setVisibility(View.VISIBLE);
                    if (isLocked) {
                        btnStopLocation.setEnabled(false);
                        btnStopLocation.setText("Đã dừng");
                        btnStopLocation.setOnClickListener(null);
                    } else {
                        btnStopLocation.setEnabled(true);
                        btnStopLocation.setText("Dừng phát định vị");
                        btnStopLocation.setOnClickListener(v -> {
                            message.setLocationLocked(true);
                            persistLocationLocked(itemView.getContext(), message);
                            int pos = getBindingAdapterPosition();
                            if (pos != RecyclerView.NO_POSITION) {
                                notifyItemChanged(pos);
                            }
                        });
                    }
                }
            } else {
                if (imgContent != null) imgContent.setVisibility(View.GONE);
                if (txtMessage != null) {
                    txtMessage.setVisibility(View.VISIBLE);
                    txtMessage.setText(message.getContent());
                    txtMessage.setOnClickListener(null);
                }
                if (btnStopLocation != null) {
                    btnStopLocation.setVisibility(View.GONE);
                }
            }

            if (txtTime != null && message.getTimestamp() != 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                txtTime.setText(sdf.format(new Date(message.getTimestamp())));
            }

            if (imgStatusPending != null) {
                boolean showPending = message.isMe() &&
                        Message.STATUS_PENDING.equalsIgnoreCase(message.getStatus());
                imgStatusPending.setVisibility(showPending ? View.VISIBLE : View.GONE);
            }

            if (imgStatusSending != null) {
                boolean showSending = message.isMe() &&
                        Message.STATUS_SENDING.equalsIgnoreCase(message.getStatus());
                imgStatusSending.setVisibility(showSending ? View.VISIBLE : View.GONE);
                if (showSending) {
                    imgStatusSending.startAnimation(sendingRotateAnimation);
                } else {
                    imgStatusSending.clearAnimation();
                }
            }

            if (imgStatusPending != null && imgStatusPending.getVisibility() != View.VISIBLE) {
                imgStatusPending.clearAnimation();
            }
        }
    }
}