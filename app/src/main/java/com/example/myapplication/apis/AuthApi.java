package com.example.myapplication.apis;

import com.example.myapplication.models.Client;
import com.example.myapplication.models.LoginRequest;
import com.example.myapplication.models.SignupRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {

    @POST("client/login")
    Call<Client> login(@Body LoginRequest request);

    @POST("signup")
    Call<Client> signup(@Body SignupRequest request);
}
