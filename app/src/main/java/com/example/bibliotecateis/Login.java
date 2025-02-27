package com.example.bibliotecateis;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import com.example.bibliotecateis.API.models.User;
import com.example.bibliotecateis.API.repository.BookRepository;
import com.example.bibliotecateis.API.repository.UserRepository;
import java.util.List;

public class Login extends AppCompatActivity {

    private Button btnLogin;
    private EditText etContrasena, etUsuario;
    private UserRepository userRepository;
    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        inicializar();

        btnLogin.setOnClickListener(v -> {
            String usuario = etUsuario.getText().toString();
            String contrasena = etContrasena.getText().toString();
            inicioSesion(usuario, contrasena);
        });
    }

    private void inicializar() {
        btnLogin = findViewById(R.id.btnLogin);
        etContrasena = findViewById(R.id.etContrasena);
        etUsuario = findViewById(R.id.etUsuario);
    }

    private void inicioSesion(String usuario, String password) {

        userRepository = new UserRepository();
        userRepository.getUsers(new BookRepository.ApiCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> result) {
                for(User user : result) {
                    if(user.getEmail().equals(usuario) && user.getPasswordHash().equals(password)) {
                        userViewModel = new ViewModelProvider(Login.this).get(UserViewModel.class);
                        userViewModel.actualizarUser(user);
                        Toast.makeText(Login.this, "Sesión iniciada", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Login.this, listadoLibros.class);
                        startActivity(intent);
                    }
                }
                Toast.makeText(Login.this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(Login.this, "Error al buscar los usuarios", Toast.LENGTH_SHORT).show();
            }
        });
    }
}