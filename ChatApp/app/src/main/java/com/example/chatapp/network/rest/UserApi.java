package com.example.chatapp.network.rest;

import com.example.chatapp.model.User;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface UserApi {
    // Gọi API: GET /api/users?excludeId=...
    @GET("api/users")
    Call<List<User>> getAllUsers(@Query("excludeId") Integer excludeId);
}