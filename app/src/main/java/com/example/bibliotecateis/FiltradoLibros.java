package com.example.bibliotecateis;

import android.util.Log;
import android.widget.Toast;

import com.example.bibliotecateis.API.models.Book;
import com.example.bibliotecateis.API.models.BookLending;
import com.example.bibliotecateis.API.models.User;
import com.example.bibliotecateis.API.repository.BookRepository;

import java.util.ArrayList;
import java.util.List;

public class FiltradoLibros {

    private BookRepository bookRepository = new BookRepository();



    public List<Book> buscarPrestadosUsuario(User user){
        List<Book> prestados = new ArrayList<>();
        for(BookLending bookLending : user.getBookLendings()){
            bookRepository.getBookById(bookLending.getBookId(),new BookRepository.ApiCallback<Book>() {

                @Override
                public void onSuccess(Book result) {
                    prestados.add(result);
                }

                @Override
                public void onFailure(Throwable t) {
                    Log.e("BookRepository", "Error deleting book", t);
                }
            });
        }
        return prestados;
    }

}