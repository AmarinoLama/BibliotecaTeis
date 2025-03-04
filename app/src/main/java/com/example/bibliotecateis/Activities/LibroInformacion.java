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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.bibliotecateis.API.models.Book;
import com.example.bibliotecateis.API.repository.BookRepository;
import com.example.bibliotecateis.Helpers;
import com.example.bibliotecateis.Login.Login;
import com.example.bibliotecateis.R;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_libro_informacion);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        inicializar();

        // Obtenemos el ID del usuario y del libro mediante los shared preferences y el intent para pasar información entre clases
        userId = getSharedPreferences(Login.SHARED_PREFERENCES, MODE_PRIVATE).getInt(USER_ID, 0);
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

        // Listener del botón prestar que llama al método prestarLibro de la clase Helpers y recarga la página para actualizar los valores
        btnPrestar.setOnClickListener(v -> {
            Helpers.prestarLibro(userId, bookId);
            Intent i = new Intent(LibroInformacion.this, LibroInformacion.class);
            i.putExtra(BOOK_ID_EXTRA, bookId);
            startActivity(i);
        });

        // Listener del botón devolver que llama al método devolverLibro de la clase Helpers y recarga la página para actualizar los valores
        btnDevolver.setOnClickListener(v -> {
            Helpers.devolverLibro(bookId);
            Intent i = new Intent(LibroInformacion.this, LibroInformacion.class);
            i.putExtra(BOOK_ID_EXTRA, bookId);
            startActivity(i);
        });

        // Listener del botón volver que nos lleva a la pantalla de listado de libros
        btnVolver.setOnClickListener(v -> {
            Intent i = new Intent(LibroInformacion.this, ListadoLibros.class);
            startActivity(i);
        });

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
                // Actualizamos la información en pantalla
                tvTitulo.setText(result.getTitle());
                tvIsbn.setText(result.getIsbn());
                tvAutor.setText(result.getAuthor());

                // Cargamos la imagen de la portada en el caso de que no se encuentre vacía
                if (!result.getBookPicture().isEmpty()) {
                    Helpers.cargarImagen(result.getBookPicture(), ivPortada);
                }

                // Cargamos las existencias y disponibilidad
                Helpers.getNextDevolucion(result, tvProximoDisponible);
                Helpers.obtenerExistencias(result, tvLibrosExistentes, tvLibrosDisponibles, new Object[]{btnPrestar,btnDevolver,userId});

                // El método de obtenerExistencias se encarga de cargar la información de los libros existentes y disponibles y de habilitar o deshabilitar los botones de prestar y devolver, se han juntado estas acciones en el mismo método, ya que la información de los libros disponibles o no se necesita para cargar los botones
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(LibroInformacion.this,
                        "Error al buscar el libro",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}