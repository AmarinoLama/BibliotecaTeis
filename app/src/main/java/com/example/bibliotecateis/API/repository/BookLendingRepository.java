package com.example.bibliotecateis.API.repository;

import android.util.Log;
import com.example.bibliotecateis.API.models.BookLending;
import com.example.bibliotecateis.API.retrofit.ApiService;
import com.example.bibliotecateis.API.retrofit.ApiClient;

import java.io.IOException;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookLendingRepository {

    private ApiService apiService;

    public BookLendingRepository() {
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    public void getAllLendings(final BookRepository.ApiCallback<List<BookLending>> callback) {
        apiService.getLendings().enqueue(new Callback<List<BookLending>>() {
            @Override
            public void onResponse(Call<List<BookLending>> call, Response<List<BookLending>> response) {
                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<List<BookLending>> call, Throwable t) {
                Log.e("BookLendingRepository", "Error fetching lendings", t);
                callback.onFailure(t);
            }
        });
    }

    // Se ha modificado este método porque por algún motivo no funcionaba de la otra forma y como algunos de nuestros compañeros lo tenían así, hemos decidido cambiarlo

    public void lendBook(int userId, int bookId, final BookRepository.ApiCallback<Boolean> callback) {
        apiService.lendBook(userId, bookId).enqueue(new Callback<BookLending>() {
            @Override
            public void onResponse(Call<BookLending> call, Response<BookLending> response) {
                callback.onSuccess(response.isSuccessful());
            }

            @Override
            public void onFailure(Call<BookLending> call, Throwable t) {
                Log.e("BookLendingRepository", "Error lending book", t);
                callback.onFailure(t);
            }
        });
    }


    public void returnBook(int id, final BookRepository.ApiCallback<Boolean> callback) {
        apiService.returnBook(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                callback.onSuccess(response.isSuccessful());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("BookLendingRepository", "Error returning book", t);
                callback.onFailure(t);
            }
        });
    }
}