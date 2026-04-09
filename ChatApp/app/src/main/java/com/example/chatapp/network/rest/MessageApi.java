package com.example.chatapp.network.rest;

import com.example.chatapp.model.ChatGroup;
import com.example.chatapp.model.Conversation;
import com.example.chatapp.model.Message;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MessageApi {
    @GET("api/messages/history")
    Call<List<Message>> getHistory(@Query("u1") Integer u1, @Query("u2") Integer u2);

    @GET("api/messages/conversations/{userId}")
    Call<List<Conversation>> getConversations(@Path("userId") Integer userId);

    @Multipart
    @POST("api/messages/upload")
    Call<ResponseBody> uploadFile(@Part MultipartBody.Part file);

    @Multipart
    @POST("api/messages/upload/video")
    Call<ResponseBody> uploadVideo(@Part MultipartBody.Part file);

    @GET("api/groups/my-groups")
    Call<List<ChatGroup>> getMyGroups(@Query("userId") Integer userId);

    @POST("api/groups/create")
    Call<GroupResponse> createGroup(@Body CreateGroupRequest request);

    @GET("api/messages/group/{groupId}")
    Call<List<Message>> getGroupHistory(@Path("groupId") Integer groupId);
}
