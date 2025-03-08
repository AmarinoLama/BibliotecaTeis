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
import com.example.bibliotecateis.Login.Login;
import com.example.bibliotecateis.R;
import com.example.bibliotecateis.viewModels.BookViewModel;
import com.example.bibliotecateis.viewModels.BooksViewModel;

import java.util.List;

public class LibroInformacion extends AppCompatActivity {

    public static String USER_ID = "userId";
    public static String BOOK_ID_EXTRA = "bookId";

    private TextView tvTitulo, tvIsbn, tvAutor, tvLibrosDisponibles, tvProximoDisponible, tvLibrosExistentes;
    private ImageView ivPortada;
    private Button btnPrestar, btnDevolver, btnVolver;
    private Toolbar tb;

    private String[] isbnEscaneado = new String[]{""};

    public int userId = 0;
    public int bookId = 0;

    private BookViewModel bookViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_libro_informacion);

        inicializar();

        userId = getSharedPreferences(Login.SHARED_PREFERENCES, MODE_PRIVATE).getInt(USER_ID, 0);
        bookId = getIntent().getIntExtra(BOOK_ID_EXTRA, 0);
        cargarInfoLibro(bookId);

        // Inicializamos el QR Launcher para poder escanear:
        Helpers.inicializarQRLauncher(this, isbnEscaneado, result -> {
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
        btnPrestar.setOnClickListener(v -> {
            Helpers.prestarLibro(userId, bookId);
            cargarInfoLibro(bookId);
        });

        btnDevolver.setOnClickListener(v -> {
            Helpers.devolverLibro(bookId);
            cargarInfoLibro(bookId);
        });

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

    public void cargarInfoLibro(Integer id) {
        BookRepository bookRepository = new BookRepository();
        bookRepository.getBookById(id, new BookRepository.ApiCallback<Book>() {
            @Override
            public void onSuccess(Book result) {
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

