package com.example.bibliotecateis.Activities;

import static com.example.bibliotecateis.Helpers.cargarToolbar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.bibliotecateis.API.models.Book;
import com.example.bibliotecateis.API.repository.BookRepository;
import com.example.bibliotecateis.Helpers;
import com.example.bibliotecateis.R;
import com.example.bibliotecateis.viewModels.BookViewModel;
import com.example.bibliotecateis.viewModels.BooksViewModel;

import java.util.List;

// Clase que muestra la información de un libro en particular

public class LibroInformacion extends AppCompatActivity {

    // Constantes para el manejo de SharedPreferences
    public static final String USER_ID = "userId";
    public static final String BOOK_ID_EXTRA = "bookId";

    private TextView tvTitulo, tvIsbn, tvAutor, tvLibrosDisponibles, tvProximoDisponible, tvLibrosExistentes;
    private ImageView ivPortada;
    private Button btnPrestar, btnDevolver, btnVolver;
    private Toolbar tb;

    // Variable donde se almacena el resultado de lo escaneado en el QR
    private String[] isbnEscaneado = new String[]{""};

    // Variables para almacenar los ids del libro y del usuario
    public int userId = 0;
    public int bookId = 0;

    private BookViewModel bookViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_libro_informacion);

        inicializar();

        // Obtenemos el ID del usuario y del libro mediante los shared preferences y el intent para pasar información entre clases
        userId = Helpers.getUser(this);
        bookId = getIntent().getIntExtra(BOOK_ID_EXTRA, 0);

        // Cargamos la información del libro a partir de la id de este
        cargarInfoLibro(bookId);

        // Inicializamos el QR Launcher para poder escanear:
        Helpers.inicializarQRLauncher(this, isbnEscaneado, result -> {

            // Como mencioné en la clase Helpers este método es como un listener el cual puedes añadirle acciones cuando lea el QR

            isbnEscaneado[0] = result;
            System.out.println("ISBN escaneado desde LibroInformacion: " + isbnEscaneado[0]);

            // Una vez tenemos el ISBN, buscamos el libro en la lista general:
            BookRepository bookRepository = new BookRepository();
            bookRepository.getBooks(new BookRepository.ApiCallback<List<Book>>() {
                @Override
                public void onSuccess(List<Book> books) {
                    for (Book book : books) {
                        if (book.getIsbn().equals(isbnEscaneado[0])) {
                            bookId = book.getId();
                            System.out.println("Libro hallado con ISBN: " + book.getIsbn() + " => ID: " + book.getId());

                            cargarInfoLibro(bookId);
                            return;
                        }
                    }
                    Toast.makeText(LibroInformacion.this, "No se encontró el libro con ISBN: " + isbnEscaneado[0], Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Throwable t) {
                    Toast.makeText(LibroInformacion.this,
                            "Error al buscar libros: " + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        });

        setOCL();

        cargarToolbar(this, tb);

        //cargarBotones();

    }

    private void setOCL() {
        // Listeners de botones:
        // Listener del botón prestar que llama al método prestarLibro de la clase Helpers y recarga la página para actualizar los valores
        btnPrestar.setOnClickListener(v -> {
            Helpers.prestarLibro(userId, bookId);
            cargarInfoLibro(bookId);
        });

        // Listener del botón devolver que llama al método devolverLibro de la clase Helpers y recarga la página para actualizar los valores
        btnDevolver.setOnClickListener(v -> {
            Helpers.devolverLibro(bookId);
            cargarInfoLibro(bookId);
        });

        // Listener del botón volver que nos lleva a la pantalla de listado de libros
        btnVolver.setOnClickListener(v -> {
            Intent i = new Intent(LibroInformacion.this, ListadoLibros.class);
            startActivity(i);
        });
    }

    private void cargarVM(Book book) {

        bookViewModel = new ViewModelProvider(this).get(BookViewModel.class);
        bookViewModel.getBook().observe(this, new Observer<Book>() {
            @Override
            public void onChanged(Book book) {
                // Update the UI directly instead of calling cargarInfoLibro
                if (book != null) {
                    cargarLibro(book);
                }
            }
        });


        bookViewModel.setBook(book);

        // Cargamos el toolbar
        cargarToolbar(this, tb);
    }

    private void inicializar() {
        tb = findViewById(R.id.toolbar);
        tvTitulo = findViewById(R.id.tvTitulo);
        tvIsbn = findViewById(R.id.tvIsbn);
        tvAutor = findViewById(R.id.tvAutor);
        tvLibrosDisponibles = findViewById(R.id.tvLibrosDisponibles);
        tvProximoDisponible = findViewById(R.id.tvProximoDisponible);
        tvLibrosExistentes = findViewById(R.id.tvLibrosExistentes);
        ivPortada = findViewById(R.id.ivPortada);
        btnPrestar = findViewById(R.id.btnPrestar);
        btnDevolver = findViewById(R.id.btnDevolver);
        btnVolver = findViewById(R.id.btnVolver);
    }

    // Función que se encarga de cargar toda la información del libro a partir de su id

    public void cargarInfoLibro(Integer id) {
        BookRepository bookRepository = new BookRepository();
        // Se usa el método de getBookById para obtener la información del libro
        bookRepository.getBookById(id, new BookRepository.ApiCallback<Book>() {
            @Override
            public void onSuccess(Book result) {
                // Control de errores por si no se encuentra el libro
                if (result == null) {
                    Toast.makeText(LibroInformacion.this, "El repositorio devolvió null para el ID: " + id, Toast.LENGTH_LONG).show();
                    return;
                }

                cargarVM(result);



                // Ajustamos visibilidad de botones (Prestar/Devolver) según estado
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(LibroInformacion.this,
                        "Error al buscar el libro",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarLibro(Book result) {
        // Actualizamos la información en pantalla
        tvTitulo.setText(result.getTitle());
        tvIsbn.setText(result.getIsbn());
        tvAutor.setText(result.getAuthor());

        if (result.getBookPicture() == null || !result.getBookPicture().isEmpty()) {
            Helpers.cargarImagen(result.getBookPicture(), ivPortada);
        }

        // Cargamos las existencias y disponibilidad
        Helpers.getNextDevolucion(result, tvProximoDisponible);
        Helpers.obtenerExistencias(result, tvLibrosExistentes, tvLibrosDisponibles, new Object[]{btnPrestar,btnDevolver,userId});
    }


}