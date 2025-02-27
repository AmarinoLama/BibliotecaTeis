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
        abrirMenuPrincipal();

    }

    private void abrirLogin() {
        Intent intent = new Intent(MainActivity.this, Login.class);
        startActivity(intent);
    }

    private void abrirListadoLibros() {
        Intent intent = new Intent(MainActivity.this, ListadoLibros.class);
        startActivity(intent);
    }

    private void abrirLibroInformacion() {
        Intent intent = new Intent(MainActivity.this, LibroInformacion.class);
        startActivity(intent);
    }

    private void abrirMenuPrincipal() {
        Intent intent = new Intent(MainActivity.this, MenuPrincipal.class);
        startActivity(intent);
    }
}