package com.example.chatapp.network.rest;

import com.example.chatapp.model.User;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call; // PHẢI LÀ DÒNG NÀY
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

        // 1. Lấy danh sách tất cả người dùng để kết bạn
        // Backend hiện có: GET /api/users?excludeId={myId}
        @GET("api/users")
        Call<List<User>> getAllUsers(@Query("excludeId") int excludeId);

        // 2. Gửi lời mời kết bạn (Dùng cho nút "Kết bạn")
        @POST("api/friends/send-request")
        Call<ResponseBody> sendFriendRequest(
                        @Query("senderId") int senderId,
                        @Query("receiverId") int receiverId);

        // 3. Chấp nhận kết bạn
        @POST("api/friends/accept-request")
        Call<ResponseBody> acceptFriendRequest(
                        @Query("requestId") int requestId);
}