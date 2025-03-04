package com.example.bibliotecateis;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Random;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuProvider;

import com.example.bibliotecateis.API.models.Book;
import com.example.bibliotecateis.API.models.BookLending;
import com.example.bibliotecateis.API.repository.BookLendingRepository;
import com.example.bibliotecateis.API.repository.BookRepository;
import com.example.bibliotecateis.API.repository.ImageRepository;
import com.example.bibliotecateis.Activities.LibroInformacion;
import com.example.bibliotecateis.Activities.LibrosUsuario;
import com.example.bibliotecateis.EditPreferences.EditPreferences;
import com.example.bibliotecateis.Activities.ListadoLibros;
import com.example.bibliotecateis.Login.Login;
import com.example.bibliotecateis.Activities.MenuPrincipal;
import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Helpers {

    private static ActivityResultLauncher<ScanOptions> qrLauncher;
    private static BookLendingRepository bookLendingRepository = new BookLendingRepository();

    // Función que usa los métodos de la API para obtner la imagen de un libro en específico para settearlo posteriormente a la imagen que se le pasa como argumento

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

    // Función que usa los métodos de la API para obtener la cantidad de libros disponibles y existentes de un libro en específico para settearlos posteriormente a los TextView que se le pasan como argumento

    public static void obtenerExistencias(Book book, TextView existencias, TextView disponibles, @Nullable Object[] lista) {
        BookRepository bookRepository = new BookRepository();
        bookRepository.getBooks(new BookRepository.ApiCallback<List<Book>>() {
            @Override
            public void onSuccess(List<Book> result) {
                int existenciasTotal = 0;
                int existenciasDisponibles = 0;
                for (Book b : result) {
                    // Si comparte el isbn se le suma 1 a las existencias totales y a las disponibles si está disponible, ya que pueden existir varios libros repetidos
                    if (Objects.equals(b.getIsbn(), book.getIsbn())) {
                        existenciasTotal++;
                        existenciasDisponibles += b.isAvailable() ? 1 : 0;
                    }
                }
                existencias.setText(String.valueOf(existenciasTotal));
                disponibles.setText(String.valueOf(existenciasDisponibles));
                if (lista != null) {
                    cargarBotones(book, (Button) lista[0], (Button) lista[1], (int) lista[2], existenciasDisponibles);
                }
            }
            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(existencias.getContext(), "Error al obtener las existencias", Toast.LENGTH_SHORT).show();
                existencias.setText("Error");
                disponibles.setText("Error");
            }
        });
    }

    // Función que controla los botones de LibroInformación para que se activen y descativen dependiendo de si el usuario posee el libro o no

    private static void cargarBotones(Book book, Button btnPrestar, Button btnDevolver, int userId, int existenciasDisponibles) {

        BookLendingRepository bookLendingRepository = new BookLendingRepository();
        bookLendingRepository.getAllLendings(new BookRepository.ApiCallback<List<BookLending>>() {
            @Override
            public void onSuccess(List<BookLending> lendings) {
                for (BookLending bookLending : lendings) {
                    if (bookLending.getUserId() == userId
                            && Objects.equals(bookLending.getBook().getIsbn(), book.getIsbn()) && bookLending.getReturnDate() == null) {
                        // Si el usuario tiene prestado este libro
                        btnDevolver.setEnabled(true);
                        btnPrestar.setEnabled(false);
                        return;
                    }
                }
                // Si no lo tiene prestado:
                btnDevolver.setEnabled(false);
                btnPrestar.setEnabled(existenciasDisponibles > 0);
            }

            @Override
            public void onFailure(Throwable t) {
                System.out.println("Error al buscar los préstamos: " + t.getMessage());
            }
        });
    }

    // Función que coge como argumento la lista de todos los libros para luego eliminar los libros que se repiten

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

    // Funcion que devuelve una lista de libros aleatorios en función de la cantidad especificada para mostrar en el menú.
    public static List<Book> getLibrosRandom(List<Book> libros, int cantidad) {
        List<Book> librosRandom = new ArrayList<>();
        Random random = new Random();

        // Se asegura de que no haya libros con el mismo ISBN,
        // ya que pueden existir diferentes libros con el mismo ISBN pero con distintos identificadores.
        for (int i = 0; i < cantidad; ) {
            // Generamos un numero aleatorio dentro del rango de la lista
            int index = random.nextInt(libros.size());
            // Accedemos al libro en la posición generada
            Book libroSeleccionado = libros.get(index);

            // Verificamos si el ISBN del libro ya está presente en la lista de libros aleatorios
            boolean libroRepetido = false;
            for (Book libro : librosRandom) {
                if (libro.getIsbn().equals(libroSeleccionado.getIsbn())) {
                    // Si ya existe un libro con el mismo ISBN, lo marcamos como repetido,y rompemos el bucle para continuar filtrando
                    libroRepetido = true;
                    break;
                }
            }

            // Si el libro en esa posicion random no está repetido, se agrega a la lista y se incrementa el contador para pasar al siguiente
            if (!libroRepetido) {
                librosRandom.add(libroSeleccionado);
                i++;
            }
        }

        return librosRandom;
    }


    // Función que se encarga de obtener la fecha de devolución del libro

    public static void getNextDevolucion(Book book, TextView tvProximoDisponible){
        StringBuilder mensaje = new StringBuilder();
        BookLendingRepository bookLendingRepository = new BookLendingRepository();
        bookLendingRepository.getAllLendings(new BookRepository.ApiCallback<List<BookLending>>() {
            @Override
            public void onSuccess(List<BookLending> result) {
                String ultimaFecha = "";
                // Se recorren todos los préstamos para encontrar el último préstamo del libro en cuestión
                for (BookLending bookLending : result) {
                    if (bookLending.getBook().getIsbn().equals(book.getIsbn()) && bookLending.getReturnDate() == null) {
                        if (ultimaFecha.isEmpty()) {
                            ultimaFecha = bookLending.getLendDate();
                            continue;
                        }
                        if (bookLending.getBook().getIsbn().equals(book.getIsbn())) {
                            ultimaFecha = compararFechas(bookLending.getLendDate(), ultimaFecha);
                        }
                    }
                }
                // Si no se ha encontrado ninguna fecha de préstamo, se muestra un mensaje vacío
                if (ultimaFecha.isEmpty()) {
                    tvProximoDisponible.setText("");
                    return;
                }
                // Una vez encontrada la fecha se le suman 15 días para calcular la fecha de devolución
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

    // Función que compara dos fechas y devuelve la más reciente (usado en getNextDevolucion)

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

    // Función que suma una cantidad de días a una fecha (usado en getNextDevolucion)

    private static String sumarDias(String dateString,int dias) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime dateTime = LocalDateTime.parse(dateString, formatter);
        LocalDateTime newDateTime = dateTime.plusDays(dias);
        return newDateTime.format(formatter);
    }

    // Función que se encarga de cargar la toolBar con todas sus opciones

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
                if(id == R.id.btnMenuVolverLogin){
                    Intent intent = new Intent(context, Login.class);
                    context.startActivity(intent);
                }
                if(id == R.id.btnMenuCamara){
                    scanearQR();
                }
                if(id == R.id.btnMenuIrPerfilUsuario){
                    Intent intent = new Intent(context, LibrosUsuario.class);
                    context.startActivity(intent);
                }
                if(id == R.id.btnMenuPreferencias){
                    Intent intent = new Intent(context, EditPreferences.class);
                    context.startActivity(intent);
                }

                // Devuelve false si no se selecciona ningún elemento conocido
                return false;
            }
        });
    }

    // Función que se encarga de escanear un código QR, es decir, abrir la cámara para leer un QR

    public static void scanearQR() {
        if (qrLauncher != null) {
            ScanOptions options = new ScanOptions();
            options.setPrompt("Escanea el código de barras");
            options.setBeepEnabled(true);
            options.setOrientationLocked(true);
            options.setCaptureActivity(CaptureActivity.class);

            qrLauncher.launch(options);
        } else {
            System.out.println("QR Launcher no inicializado");
        }
    }

    // Función que se encarga de obtener la fecha de devolución del libro para un usuario en específico

    public static void getNextDevolucionUsuario(Book book, int userId, TextView txtFechaDevolucion) {
        StringBuilder mensaje = new StringBuilder();
        BookLendingRepository bookLendingRepository = new BookLendingRepository();
        // Se usa el getAllLendings para recorrer la lista y comprobar si el usuario coincide
        bookLendingRepository.getAllLendings(new BookRepository.ApiCallback<List<BookLending>>() {
            @Override
            public void onSuccess(List<BookLending> result) {
                String ultimaFecha = "";
                for (BookLending bookLending : result) {
                    // Cuando se encuentra una coincidencia de usuario y isbn del libro se settea la fecha de préstamo
                    if (bookLending.getBook().getIsbn().equals(book.getIsbn()) && bookLending.getReturnDate() == null && bookLending.getUserId() == userId) {
                        ultimaFecha = bookLending.getLendDate();
                    }
                }
                // Se le suman 15 días para calcular la fecha de devolución
                String fechaDevolucion = sumarDias(ultimaFecha,15);
                if(compararFechas(fechaDevolucion, String.valueOf(LocalDateTime.now()).substring(0,19)).equals(ultimaFecha)){
                    txtFechaDevolucion.setTextColor(Color.RED);
                }
                fechaDevolucion = fechaDevolucion.substring(0,fechaDevolucion.indexOf('T'));
                txtFechaDevolucion.setText("(" + fechaDevolucion + ")");
            }

            @Override
            public void onFailure(Throwable t) {
                mensaje.append("Error al buscar las devoluciones");
            }
        });
    }

    // Mini interfaz generada por nuestro compañero ChatGPT para que funcione la lógica de inicializarQRLauncher

    public interface QRCallback {
        void onResult(String scannedData);
    }

    // Función que se encarga de inicializar el escaneo de un código QR y obtener el resultado del QR

    // Se ha tenido que usar estos métodos porque por la lógica de los hilos, el hilo está en ejecución y cuando obtiene el valor del QR y se intenta devolver aún no ha cargado por eso es necesario usar este método

    public static void inicializarQRLauncher(AppCompatActivity context, String[] scannedResult, QRCallback callback) {

        // La lógica de este método nos permite hacer acciones específicas para cuando devuelva el resultado como un listener (mirar en los métodos que se usa)

        qrLauncher = context.registerForActivityResult(new ScanContract(), result -> {
            if (result.getContents() != null) {
                scannedResult[0] = result.getContents();
                callback.onResult(scannedResult[0]);
                System.out.println("ISBN escaneado desde Helpers: " + scannedResult[0]);
            }
        });
    }

    // Función que se encarga de pasar el ISBN escaneado a la vista de LibroInformación

    public static void isbnToView(AppCompatActivity context, String[] isbnEscaneado) {
        if (isbnEscaneado[0] == null || isbnEscaneado[0].isEmpty()) {
            Toast.makeText(context, "No se ha encontrado un isbn valido", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(context, LibroInformacion.class);
        intent.putExtra("SCANNED_ISBN", isbnEscaneado[0]);
        context.startActivity(intent);
    }

    // Función que se encarga de prestar un libro a partir del id del usuario y del libro

    public static void prestarLibro(int userId, int bookId) {

        // Se ha tenido que modificar bookLendingRepository.lendBook porque tal y como estaba planteado no funcionaba y tras ver el código de mis compañeros me di cuenta de que no funcionaba y por eso decidí cambiarlo

        bookLendingRepository.lendBook(userId, bookId, new BookRepository.ApiCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                System.out.println("Libro prestado: " + result);
            }

            @Override
            public void onFailure(Throwable t) {
                System.out.println("Error al prestar el libro");
            }
        });
    }

    // Función que se encarga de devolver un libro a partir del id del libro

    public static void devolverLibro(int bookId) {
        bookLendingRepository.returnBook(bookId, new BookRepository.ApiCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                System.out.println("Libro devuelto: " + result);
            }

            @Override
            public void onFailure(Throwable t) {
                System.out.println("Error al devolver el libro");
            }
        });
    }

    // Función que se encarga de obtener los libros de un usuario a partir de una lista de préstamos

    public static List<Book> getUserBooksFromLendings(List<BookLending> lendings, int userId) {
        List<Book> books = new ArrayList<>();
        for (BookLending lending : lendings) {
            if (lending.getUserId() == userId && lending.getReturnDate() == null) {
                books.add(lending.getBook());
            }
        }
        return books;
    }
}