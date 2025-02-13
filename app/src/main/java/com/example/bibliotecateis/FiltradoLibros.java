package com.example.bibliotecateis;

import android.util.Log;
import android.widget.Toast;

import com.example.bibliotecateis.API.models.Book;
import com.example.bibliotecateis.API.models.BookLending;
import com.example.bibliotecateis.API.models.User;
import com.example.bibliotecateis.API.repository.BookRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

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

//    Opcionalmente, cuando se muestren los libros disponibles se debería poder ver un
//    indicador de cuantas copias existen en total y cuantas están disponibles

    public int copiasRestantes(Book book){
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

    public String fechaMasProxima(Book book){
        String ultimaFecha = "";
        for(BookLending bookLending : book.getBookLendings()){
            ultimaFecha = obtenerFechaMasAntigua(ultimaFecha, bookLending.getLendDate());
        }
        return ultimaFecha;
    }

    public static String obtenerFechaMasAntigua(String fecha1, String fecha2) {
        SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        formatoFecha.setTimeZone(TimeZone.getTimeZone("UTC"));
        if(fecha2.isEmpty()){
            return fecha1;
        }
        if(fecha1.isEmpty()){
            return fecha2;
        }
        try {
            Date date1 = formatoFecha.parse(fecha1);
            Date date2 = formatoFecha.parse(fecha2);

            if (date1.before(date2)) {
                return fecha1;
            } else {
                return fecha2;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

}