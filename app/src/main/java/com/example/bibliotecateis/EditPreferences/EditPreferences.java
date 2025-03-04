package com.example.bibliotecateis.EditPreferences;

import static com.example.bibliotecateis.Helpers.cargarToolbar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bibliotecateis.API.models.Book;
import com.example.bibliotecateis.API.repository.BookRepository;
import com.example.bibliotecateis.Activities.LibroInformacion;
import com.example.bibliotecateis.Activities.LibrosUsuario;
import com.example.bibliotecateis.Fragments.PreferenceFragment;
import com.example.bibliotecateis.Helpers;
import com.example.bibliotecateis.R;

import java.util.List;

// Clase que se encarga de cargar los edit preferences

public class EditPreferences extends AppCompatActivity {

    private Toolbar tb;

    private String[] isbnEscaneado = new String[]{""};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_preferences);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tb = findViewById(R.id.toolbar);

        // Se inicializa el QRLauncher y se le mete toda la lógica para cuando se escanee un QR
        Helpers.inicializarQRLauncher(this, isbnEscaneado, result -> {
            isbnEscaneado[0] = result;
            System.out.println("QR Escaneado desde EditPreferences: " + isbnEscaneado[0]);

            // Cuando encuentra el libro se abre la vista detallada de este
            // Hemos intenado extraer este método pero no hemos sido capaces
            BookRepository bookRepository = new BookRepository();
            bookRepository.getBooks(new BookRepository.ApiCallback<List<Book>>() {
                @Override
                public void onSuccess(List<Book> books) {
                    for (Book b : books) {
                        if (b.getIsbn().equals(isbnEscaneado[0])) {
                            Intent i = new Intent(EditPreferences.this, LibroInformacion.class);
                            i.putExtra(LibroInformacion.BOOK_ID_EXTRA, b.getId());
                            startActivity(i);
                            return;
                        }
                    }
                    Toast.makeText(EditPreferences.this,
                            "No se encontró el libro con ISBN: " + isbnEscaneado[0],
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Throwable t) {
                    Toast.makeText(EditPreferences.this,
                            "Error al cargar los libros: " + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        });

        cargarToolbar(this,tb);

        // Gracias a estas líneas de abajo podemos cargar los fragmentos de las preferencias

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.preferences_container, new PreferenceFragment())
                .commit();
    }
}