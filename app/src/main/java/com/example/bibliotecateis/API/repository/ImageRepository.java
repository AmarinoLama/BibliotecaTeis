package com.example.bibliotecateis.API.repository;

import com.example.bibliotecateis.API.retrofit.ApiClient;
import com.example.bibliotecateis.API.retrofit.ApiService;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImageRepository {

    private ApiService apiService;

    public ImageRepository() {
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    public void getImage(String imageName, final BookRepository.ApiCallback<byte[]> callback){
        apiService.getImage(imageName).enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    callback.onSuccess(response.body().bytes());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                callback.onFailure(t);
            }
        });
    }
}
