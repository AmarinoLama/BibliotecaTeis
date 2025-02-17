package com.example.bibliotecateis;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bibliotecateis.API.models.Book;
import com.example.bibliotecateis.API.repository.BookRepository;
import com.example.bibliotecateis.API.repository.ImageRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Helpers {

    // ESTA CLASE ME DIJO EL PROFE QUE LA CREE, NO TE ENFADES VICTOR <3

    public static void cargarImagen(String bookPicture, ImageView portada) {
        ImageRepository imageRepository = new ImageRepository();
        imageRepository.getImage(bookPicture, new BookRepository.ApiCallback<byte[]>() {
            @Override
            public void onSuccess(byte[] result) {
                if (result != null && result.length > 0) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(result, 0, result.length);
                    portada.setImageBitmap(bitmap);
                }
            }
            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(portada.getContext(), "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void obtenerExistencias(Book book, TextView existencias, TextView disponibles) {
        BookRepository bookRepository = new BookRepository();
        final Integer[] existenciasTotal = {0};
        final Integer[] existenciasDisponibles = {0};
        bookRepository.getBooks(new BookRepository.ApiCallback<List<Book>>() {
            @Override
            public void onSuccess(List<Book> result) {
                for (Book b : result) {
                    if (b.getIsbn() == book.getIsbn()) {
                        existenciasTotal[0]++;
                        existenciasDisponibles[0] += b.isAvailable() ? 1 : 0;
                    }
                }
                existencias.setText(existenciasTotal[0].toString());
                disponibles.setText(existenciasDisponibles[0].toString());
            }
            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(existencias.getContext(), "Error al obtener las existencias", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
