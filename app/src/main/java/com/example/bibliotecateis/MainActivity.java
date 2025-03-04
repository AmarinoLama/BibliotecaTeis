package com.example.bibliotecateis;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bibliotecateis.Activities.LibroInformacion;
import com.example.bibliotecateis.Activities.ListadoLibros;
import com.example.bibliotecateis.Login.Login;
import com.example.bibliotecateis.Activities.MenuPrincipal;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Intent intent = new Intent(MainActivity.this, Login.class);
        startActivity(intent);

    }
}