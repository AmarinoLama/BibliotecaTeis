package com.example.bibliotecateis;

import android.widget.Toast;

import com.example.bibliotecateis.API.models.Book;
import com.example.bibliotecateis.API.repository.BookRepository;

import java.util.ArrayList;
import java.util.List;

public class FiltradoLibros {

    private BookRepository bookRepository = new BookRepository();

    public List<Book> getLibrosDisponibles() {

        List<Book> librosDisponibles = new ArrayList<Book>();

        bookRepository.getBooks(new BookRepository.ApiCallback<List<Book>>() {
            @Override
            public void onSuccess(List<Book> result) {
                for (Book book : result) {
                    if (book.isAvailable()) {
                        librosDisponibles.add(book);
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
                System.out.println("Error al obtener los libros" + t);
                //Toast.makeText(MainActivity.this, "Mensaje de bienvenida", Toast.LENGTH_SHORT).show();
            }
        });

        return librosDisponibles;

    }

    public List<Book> getLibrosByAutor(String autor) {

        List<Book> librosXAutor = new ArrayList<Book>();

        bookRepository.getBooks(new BookRepository.ApiCallback<List<Book>>() {
            @Override
            public void onSuccess(List<Book> result) {
                for (Book book : result) {
                    if (book.getAuthor() == autor) {
                        librosXAutor.add(book);
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
                System.out.println("Error al obtener los libros" + t);
                //Toast.makeText(MainActivity.this, "Mensaje de bienvenida", Toast.LENGTH_SHORT).show();
            }
        });

        return librosXAutor;

    }

    public List<Book> getLibrosByTitulo(String titulo) {

        List<Book> librosXTitulo = new ArrayList<Book>();

        bookRepository.getBooks(new BookRepository.ApiCallback<List<Book>>() {
            @Override
            public void onSuccess(List<Book> result) {
                for (Book book : result) {
                    if (book.getTitle() == titulo) {
                        librosXTitulo.add(book);
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
                System.out.println("Error al obtener los libros" + t);
                //Toast.makeText(MainActivity.this, "Mensaje de bienvenida", Toast.LENGTH_SHORT).show();
            }
        });

        return librosXTitulo;

    }

}