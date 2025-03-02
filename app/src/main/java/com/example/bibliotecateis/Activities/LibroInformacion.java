package com.example.bibliotecateis.Activities;

import static com.example.bibliotecateis.Helpers.cargarToolbar;
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
import com.example.bibliotecateis.API.models.BookLending;
import com.example.bibliotecateis.API.repository.BookLendingRepository;
import com.example.bibliotecateis.API.repository.BookRepository;
import com.example.bibliotecateis.Helpers;
import com.example.bibliotecateis.Login.Login;
import com.example.bibliotecateis.R;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

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
        userId = getSharedPreferences(Login.SHARED_PREFERENCES, MODE_PRIVATE).getInt(USER_ID, 0);
        bookId = getIntent().getIntExtra(BOOK_ID_EXTRA, 0);
        cargarInfoLibro(bookId);

        Helpers.inicializarQRLauncher(this, isbnEscaneado, result -> {
            isbnEscaneado[0] = result;
            System.out.println("ISBN escaneado desde LibroInformacion: " + isbnEscaneado[0]);
            // Si quiero meterle un método para usar el result ponerlo aquí
        });

        btnPrestar.setOnClickListener(v -> {
            //Helpers.scanearQR();
            Helpers.prestarLibro(userId, bookId);
            btnPrestar.setEnabled(false);
            btnDevolver.setEnabled(true);
            tvLibrosDisponibles.setText(String.valueOf(Integer.parseInt(tvLibrosDisponibles.getText().toString()) - 1));
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime futureDate = now.plusDays(15);
            tvProximoDisponible.setText("(Devolución: " + futureDate.toLocalDate() + ")");
        });

        btnDevolver.setOnClickListener(v -> {
            Helpers.devolverLibro(bookId);
            btnDevolver.setEnabled(false);
            btnPrestar.setEnabled(true);
            tvLibrosDisponibles.setText(String.valueOf(Integer.parseInt(tvLibrosDisponibles.getText().toString()) + 1));
            tvProximoDisponible.setText("");
        });

        btnVolver.setOnClickListener(v -> {
            finish();
        });

        cargarToolbar(this,tb);

        //cargarBotones();
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

    private void cargarInfoLibro(Integer id) {
        BookRepository bookRepository = new BookRepository();
        bookRepository.getBookById(id, new BookRepository.ApiCallback<Book>() {
            @Override
            public void onSuccess(Book result) {
                tvTitulo.setText(result.getTitle());
                tvIsbn.setText(result.getIsbn());
                tvAutor.setText(result.getAuthor());
                if (!result.getBookPicture().isEmpty()) {
                    Helpers.cargarImagen(result.getBookPicture(), ivPortada);
                }
                Helpers.obtenerExistencias(result, tvLibrosExistentes, tvLibrosDisponibles);
                Helpers.getNextDevolucion(result,tvProximoDisponible);
                cargarBotones(result);

            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(LibroInformacion.this, "Error al buscar el libro", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarBotones(Book book) {

        BookLendingRepository bookLendingRepository = new BookLendingRepository();
        bookLendingRepository.getAllLendings(new BookRepository.ApiCallback<List<BookLending>>() {
            @Override
            public void onSuccess(List<BookLending> lendings) {
                for (BookLending bookLending : lendings) {
                    if (bookLending.getUserId() == userId && Objects.equals(bookLending.getBook().getIsbn(), book.getIsbn())) {
                        btnDevolver.setEnabled(true);
                        btnPrestar.setEnabled(false);
                        return;
                    }
                }
                btnDevolver.setEnabled(false);
                btnPrestar.setEnabled(true);
            }
            @Override
            public void onFailure(Throwable t) {
                System.out.println("Error al buscar los prestamos");
            }
        });
    }

}