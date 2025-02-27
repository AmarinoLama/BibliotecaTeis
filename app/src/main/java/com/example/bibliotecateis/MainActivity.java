package com.example.bibliotecateis;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        abrirLogin();

        //abrirListadoLibros();

        //abrirLibroInformacion();

    }

    private void abrirLogin() {
        Intent intent = new Intent(MainActivity.this, Login.class);
        startActivity(intent);
    }

    private void abrirListadoLibros() {
        Intent intent = new Intent(MainActivity.this, listadoLibros.class);
        startActivity(intent);
    }

    private void abrirLibroInformacion() {
        Intent intent = new Intent(MainActivity.this, LibroInformacion.class);
        startActivity(intent);
    }
}