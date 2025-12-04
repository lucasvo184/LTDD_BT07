package com.example.a22110180_bt07.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @Multipart
    @POST("updateimages.php")
    fun uploadImage(
        @Part("id") id: RequestBody,
        @Part image: MultipartBody.Part
    ): Call<UploadResponse>

    @FormUrlEncoded
    @POST("updateprofile.php")
    fun updateProfile(
        @Field("id") userId: Int,
        @Field("fullname") fullName: String,
        @Field("email") email: String,
        @Field("gender") gender: String
    ): Call<ProfileUpdateResponse>
}

data class UploadResponse(
    val success: Boolean,
    val message: String,
    val imageUrl: String?
)

data class ProfileUpdateResponse(
    val success: Boolean,
    val message: String,
    val data: UserProfile?
)

data class UserProfile(
    val id: Int,
    val username: String,
    val fullname: String,
    val email: String,
    val gender: String,
    val imageUrl: String?
)

