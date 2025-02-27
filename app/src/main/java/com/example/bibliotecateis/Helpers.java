package com.example.bibliotecateis;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bibliotecateis.API.models.Book;
import com.example.bibliotecateis.API.models.BookLending;
import com.example.bibliotecateis.API.models.User;
import com.example.bibliotecateis.API.repository.BookLendingRepository;
import com.example.bibliotecateis.API.repository.BookRepository;
import com.example.bibliotecateis.API.repository.ImageRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        bookRepository.getBooks(new BookRepository.ApiCallback<List<Book>>() {
            @Override
            public void onSuccess(List<Book> result) {
                int existenciasTotal = 0;
                int existenciasDisponibles = 0;
                for (Book b : result) {
                    if (Objects.equals(b.getIsbn(), book.getIsbn())) {
                        existenciasTotal++;
                        existenciasDisponibles += b.isAvailable() ? 1 : 0;
                    }
                }
                existencias.setText(String.valueOf(existenciasTotal));
                disponibles.setText(String.valueOf(existenciasDisponibles));
            }
            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(existencias.getContext(), "Error al obtener las existencias", Toast.LENGTH_SHORT).show();
                existencias.setText("Error");
                disponibles.setText("Error");
            }
        });
    }

    public static List<Book> getLibrosSinRepetir(List<Book> libros) {
        List<Book> librosSinRepetir = new ArrayList<>();
        for (Book libro : libros) {
            boolean repetido = false;
            for (Book libroSinRepetir : librosSinRepetir) {
                if (Objects.equals(libro.getIsbn(), libroSinRepetir.getIsbn())) {
                    repetido = true;
                    break;
                }
            }
            if (!repetido) {
                librosSinRepetir.add(libro);
            }
        }
        return librosSinRepetir;
    }

    public static void getNextDevolucion(Book book, TextView tvProximoDisponible){
        StringBuilder mensaje = new StringBuilder();
        BookLendingRepository bookLendingRepository = new BookLendingRepository();
        bookLendingRepository.getAllLendings(new BookRepository.ApiCallback<List<BookLending>>() {
            @Override
            public void onSuccess(List<BookLending> result) {
                String ultimaFecha = "";
                for (BookLending bookLending : result) {
                    if (ultimaFecha.isEmpty()) {
                        ultimaFecha = bookLending.getLendDate();
                        continue;
                    }
                    if (bookLending.getBook().getIsbn().equals(book.getIsbn())) {
                        ultimaFecha = compararFechas(bookLending.getLendDate(),ultimaFecha);
                    }
                }
                tvProximoDisponible.setText("(" + sumarDias(ultimaFecha,15) + ")");
            }

            @Override
            public void onFailure(Throwable t) {
                mensaje.append("Error al buscar las devoluciones");
            }
        });
    }

    private static String compararFechas(String lendDate, String ultimaFecha) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime date1 = LocalDateTime.parse(lendDate, formatter);
        LocalDateTime date2 = LocalDateTime.parse(ultimaFecha, formatter);

        if (date1.isAfter(date2)) {
            return lendDate;
        } else {
            return ultimaFecha;
        }
    }

    private static String sumarDias(String dateString,int dias) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime dateTime = LocalDateTime.parse(dateString, formatter);
        LocalDateTime newDateTime = dateTime.plusDays(dias);
        return newDateTime.format(formatter);
    }

    public static List<BookLending> getLendingsUser(User user) {
        List<BookLending> lendingsUser = new ArrayList<>();
        BookLendingRepository bookLendingRepository = new BookLendingRepository();
        bookLendingRepository.getAllLendings(new BookRepository.ApiCallback<List<BookLending>>() {
            @Override
            public void onSuccess(List<BookLending> result) {
                for (BookLending bookLending : result) {
                    if (bookLending.getUser().getId() == user.getId()) {
                        lendingsUser.add(bookLending);
                    }
                }
            }
            @Override
            public void onFailure(Throwable t) {
                System.out.println("Error al buscar los prestamos");
            }
        });
        return lendingsUser;
    }

    public static boolean userHasBook(User user, String bookIsbn) {
        List<BookLending> lendingsUser = getLendingsUser(user);
        for (BookLending bookLending : lendingsUser) {
            if (bookLending.getBook().getIsbn().equals(bookIsbn)) {
                System.out.println("El usuario tiene el libro");
                return true;
            }
        }
        return false;
    }
}