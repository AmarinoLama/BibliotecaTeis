package com.example.listados;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bibliotecateis.API.models.Book;

import java.util.ArrayList;
import java.util.List;

public class BookViewModel extends ViewModel {
    private final MutableLiveData<List<Book>> booksLiveData;

    public BookViewModel(List<Book> animales) {
        booksLiveData = new MutableLiveData<>(animales);
    }

    public LiveData<List<Book>> getAnimales() {
        return booksLiveData;
    }

    public void actualizarBook(int position, Book book) {
        List<Book> books = new ArrayList<>(booksLiveData.getValue());
        books.set(position, book);
        booksLiveData.setValue(books);
    }
}