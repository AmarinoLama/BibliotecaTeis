package com.example.bibliotecateis;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bibliotecateis.API.models.Book;
import com.example.bibliotecateis.API.models.User;

import java.util.ArrayList;
import java.util.List;

public class UserViewModel extends ViewModel {

    public UserViewModel() {
        userActive = new MutableLiveData<>();
    }
    private final MutableLiveData<User> userActive;

    public UserViewModel(User user) {
        userActive = new MutableLiveData<>(user);
    }

    public LiveData<User> getUser() {
        return userActive;
    }

    public void actualizarUser(User user) {
        userActive.setValue(user);
    }
}