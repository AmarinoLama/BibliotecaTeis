package com.example.bibliotecateis.Login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.bibliotecateis.API.models.User;

// Clase que no se usa en la aplicación, pero se deja por si se necesita en un futuro y para tenerlo de ejemplo para el examen

// Esta clase se encarga de manejar los datos de usuario creando una copia de los datos que se obtienen de la API

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