package com.example.chatapp.network.rest;

import com.example.chatapp.model.Message;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MessageApi {
    @GET("api/messages/history")
    Call<List<Message>> getHistory(@Query("u1") Integer u1, @Query("u2") Integer u2);
}
