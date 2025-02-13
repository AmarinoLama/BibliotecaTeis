package com.example.bibliotecateis;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = new Intent(MainActivity.this, listadoLibros.class);
        startActivity(i);
//        setContentView(R.layout.activity_login);


        abrirLogin();

       /*Intent intent = new Intent(MainActivity.this, LibroInformacion.class);
       startActivity(intent);*/

    }

    private void abrirLogin() {
        Intent intent = new Intent(MainActivity.this, Login.class);
        startActivity(intent);
    }
}