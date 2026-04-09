package com.example.chatapp.network.rest;

import com.example.chatapp.model.User;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface UserApi {

    // Lấy tất cả user trừ mình
    @GET("api/users")
    Call<List<User>> getAllUsers(@Query("excludeId") Integer excludeId);

    // Tìm kiếm user theo số điện thoại
    @GET("api/users/search")
    Call<User> searchByPhone(@Query("phoneNumber") String phoneNumber);
}