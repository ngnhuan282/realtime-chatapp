package com.example.chatapp.network.rest;

import com.example.chatapp.model.LoginRequest;
import com.example.chatapp.model.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {

    // Thêm public ở đây
    @POST("api/auth/login")
    public Call<LoginResponse> login(@Body LoginRequest request);
}