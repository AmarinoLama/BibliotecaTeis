// AnimalViewModel.java
package com.example.bibliotecateis.viewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bibliotecateis.API.models.Book;

import java.util.ArrayList;
import java.util.List;

public class BooksViewModel extends ViewModel {
    private final MutableLiveData<List<Book>> books;

    public BooksViewModel() {
        books = new MutableLiveData<>(new ArrayList<>());
    }

    public LiveData<List<Book>> getBooks() {
        return books;
    }

    public void setBooks(List<Book> newBooks) {
        books.setValue(newBooks);
    }

    public void addBook(Book book) {
        List<Book> currentList = books.getValue();
        if (currentList != null) {
            currentList.add(book);
            books.setValue(currentList);
        }
    }
}