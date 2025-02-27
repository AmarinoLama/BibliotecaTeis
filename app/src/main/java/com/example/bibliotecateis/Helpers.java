package com.example.bibliotecateis;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuProvider;

import com.example.bibliotecateis.API.models.Book;
import com.example.bibliotecateis.API.models.BookLending;
import com.example.bibliotecateis.API.models.User;
import com.example.bibliotecateis.API.repository.BookLendingRepository;
import com.example.bibliotecateis.API.repository.BookRepository;
import com.example.bibliotecateis.API.repository.ImageRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
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

    public static List<Book> getLibrosRandom(List<Book> libros,int cantidad){
        List<Book> librosRandom = new ArrayList<>();
        for (int i = 0; i < cantidad; i++) {
            int random = (int) (Math.random() * libros.size());
            librosRandom.add(libros.get(random));
        }
        return librosRandom;
    }

    public static void getNextDevolucion(Book book, TextView tvProximoDisponible){
        StringBuilder mensaje = new StringBuilder();
        BookLendingRepository bookLendingRepository = new BookLendingRepository();
        bookLendingRepository.getAllLendings(new BookRepository.ApiCallback<List<BookLending>>() {
            @Override
            public void onSuccess(List<BookLending> result) {
                String ultimaFecha = "";
                for (BookLending bookLending : result) {
                    if (bookLending.getBook().getIsbn().equals(book.getIsbn())){
                        if (ultimaFecha.isEmpty()) {
                            ultimaFecha = bookLending.getLendDate();
                            continue;
                        }
                        if (bookLending.getBook().getIsbn().equals(book.getIsbn())) {
                            ultimaFecha = compararFechas(bookLending.getLendDate(), ultimaFecha);
                        }
                    }
                }
                if (ultimaFecha.isEmpty()) {
                    tvProximoDisponible.setText("");
                    return;
                }
                String fechaDevolucion = sumarDias(ultimaFecha,15);
                if(compararFechas(fechaDevolucion, String.valueOf(LocalDateTime.now()).substring(0,19)).equals(ultimaFecha)){
                    tvProximoDisponible.setTextColor(Color.RED);
                }
                fechaDevolucion = fechaDevolucion.substring(0,fechaDevolucion.indexOf('T'));
                tvProximoDisponible.setText("(" + fechaDevolucion + ")");
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


    public static void cargarToolbar(AppCompatActivity context, Toolbar tb) {
        // Configura la barra de herramientas (Toolbar) en la actividad proporcionada
        context.setSupportActionBar(tb);

        // Añade un proveedor de menú a la actividad
        context.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                // Infla el menú desde el recurso XML 'main_menu'
                menuInflater.inflate(R.menu.main_menu, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                // Obtiene el ID del elemento del menú seleccionado
                int id = menuItem.getItemId();
                // Maneja la selección de los elementos del menú
                if(id == R.id.btnMenuMenuPrincipal){
                    Intent intent = new Intent(context, MenuPrincipal.class);
                    context.startActivity(intent);
                }
                if(id == R.id.btnMenuListadoLibros){
                    Intent intent = new Intent(context, ListadoLibros.class);
                    context.startActivity(intent);
                }
                if(id == R.id.btnMenuLogin){
                    Intent intent = new Intent(context, Login.class);
                    context.startActivity(intent);
                }
                if(id == R.id.btnMenuCamara){
                    // Muestra un mensaje de "Opción 3" si se selecciona 'btnMenuCamara'
                    Toast.makeText(context, "Opción 3", Toast.LENGTH_SHORT).show();
                    return true;
                }
                // Devuelve false si no se selecciona ningún elemento conocido
                return false;
            }
        });
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
