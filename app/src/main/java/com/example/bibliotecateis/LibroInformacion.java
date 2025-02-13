package com.example.bibliotecateis;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LibroInformacion extends AppCompatActivity {

    private TextView tvTitulo, tvIsbn, tvAutor, tvLibrosDisponibles, tvProximoDisponible, tvLibrosExistentes;
    private ImageView ivPortada;
    private Button btnReservar;

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

        btnReservar.setOnClickListener(v -> {
            // Reservar libro
        });
    }

    private void inicializar() {
        tvTitulo = findViewById(R.id.tvTitulo);
        tvIsbn = findViewById(R.id.tvIsbn);
        tvAutor = findViewById(R.id.tvAutor);
        tvLibrosDisponibles = findViewById(R.id.tvLibrosDisponibles);
        tvProximoDisponible = findViewById(R.id.tvProximoDisponible);
        tvLibrosExistentes = findViewById(R.id.tvLibrosExistentes);
        ivPortada = findViewById(R.id.ivPortada);
        btnReservar = findViewById(R.id.btnReservar);
    }
}