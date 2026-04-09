package com.example.chatapp.adapter;

import com.example.chatapp.model.Conversation;

/**
 * Interface xử lý click vào một cuộc hội thoại
 */
public interface OnConversationClickListener {
    void onItemClick(Conversation conversation);
}