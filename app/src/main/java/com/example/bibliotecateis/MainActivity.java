package com.example.bibliotecateis;

import android.content.Intent;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = new Intent(MainActivity.this, listadoLibros.class);
        startActivity(i);
//        setContentView(R.layout.activity_login);


        //abrirLogin();

    }

    private void abrirLogin() {
        Intent intent = new Intent(MainActivity.this, Login.class);
        startActivity(intent);
    }


}