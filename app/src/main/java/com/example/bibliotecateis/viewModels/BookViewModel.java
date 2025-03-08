// AnimalViewModel.java
package com.example.bibliotecateis.viewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bibliotecateis.API.models.Book;

import java.util.ArrayList;
import java.util.List;

public class BookViewModel extends ViewModel {
    private final MutableLiveData<Book> books;

    public BookViewModel() {
        books = new MutableLiveData<>();
    }

    public LiveData<Book> getBook() {
        return books;
    }

    public void setBook(Book newBooks) {
        books.setValue(newBooks);
    }
}