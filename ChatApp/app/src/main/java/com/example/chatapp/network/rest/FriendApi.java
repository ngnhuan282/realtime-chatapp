package com.example.chatapp.network.rest;

import com.example.chatapp.model.User;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface FriendApi {

        @GET("api/users")
        Call<List<User>> getAllUsers(@Query("excludeId") int excludeId);

        @POST("api/friends/send-request")
        Call<ResponseBody> sendFriendRequest(
                @Query("senderId") int senderId,
                @Query("receiverId") int receiverId);

        @POST("api/friends/accept-request")
        Call<ResponseBody> acceptFriendRequest(
                @Query("senderId") int senderId,
                @Query("receiverId") int receiverId);

        @GET("api/friends/pending-requests")
        Call<List<User>> getPendingRequests(@Query("userId") int userId);

        @GET("api/friends/sent-requests")
        Call<List<User>> getSentRequests(@Query("userId") int userId);

        // Thêm method này
        @GET("api/friends/my-friends")
        Call<List<User>> getMyFriends(@Query("userId") int userId);
}