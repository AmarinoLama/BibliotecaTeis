package com.example.bibliotecateis;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.bibliotecateis.API.models.Book;
import com.example.bibliotecateis.API.repository.BookRepository;
import com.example.bibliotecateis.API.repository.ImageRepository;
import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class LibroInformacion extends AppCompatActivity {


    public static String BOOK_ID_EXTRA = "bookId";
    private TextView tvTitulo, tvIsbn, tvAutor, tvLibrosDisponibles, tvProximoDisponible, tvLibrosExistentes;
    private ImageView ivPortada;
    private Button btnPrestar, btnDevolver, btnVolver;

    private ActivityResultLauncher<ScanOptions> barcodeLauncher;

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

        // Cargar información del libro
        Intent intent = getIntent();
        Integer idLibro = intent.getIntExtra(BOOK_ID_EXTRA, 0);

        cargarInfoLibro(idLibro);

        btnDevolver.setOnClickListener(v -> {
            // Devolver libro
        });

        btnVolver.setOnClickListener(v -> {
            finish();
        });

        barcodeLauncher = registerForActivityResult(new ScanContract(), result -> {
            if (result.getContents() != null) {
                String scannedData = result.getContents();
                System.out.println("QR Escaneado: " + scannedData);
            }
        });

        btnPrestar.setOnClickListener(v -> scanCode());
    }

    private void scanCode() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Escanea un código QR");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureActivity.class);

        //hacer algo q lea el codigo qr

        //harcodear el código q lee x defecto

        barcodeLauncher.launch(options);
    }

    private void inicializar() {
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
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(LibroInformacion.this, "Error al buscar el libro", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarBotones(String user) {

    }
}