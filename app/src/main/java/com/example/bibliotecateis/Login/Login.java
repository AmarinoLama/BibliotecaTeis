package com.example.bibliotecateis.Login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.bibliotecateis.API.models.User;
import com.example.bibliotecateis.API.repository.BookRepository;
import com.example.bibliotecateis.API.repository.UserRepository;
import com.example.bibliotecateis.Activities.LibroInformacion;
import com.example.bibliotecateis.Activities.MenuPrincipal;
import com.example.bibliotecateis.R;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

// Clase que se encarga de toda la gestión de la pantalla de inicio de sesión

public class Login extends AppCompatActivity {

    public static final String SHARED_PREFERENCES = "sharedPreferences";

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

    // Función que se encarga de gestionar la lógica para el inicio de sesión

    private void inicioSesion(String usuario, String password) {

        userRepository = new UserRepository();
        userRepository.getUsers(new BookRepository.ApiCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> result) {
                for(User user : result) {

                    // Si el usuario y el email coinciden con los datos de la base de datos, se inicia sesión

                    if(user.getEmail().equals(usuario) && user.getPasswordHash().equals(password)) {

                        // Antes usábamos el userViewModel para actualizar el usuario, pero ahora como no nos funcionaba y aprendimos a usar las preferencias compartidas, lo hemos comentado y sustituido por el código de abajo

                        /*userViewModel = new ViewModelProvider(Login.this).get(UserViewModel.class);
                        userViewModel.actualizarUser(user);*/

                        // Una vez iniciada la sesión, se guarda el id del usuario en las preferencias compartidas

//                        SharedPreferences sp = getSharedPreferences(Login.SHARED_PREFERENCES, MODE_PRIVATE);
//                        SharedPreferences.Editor editor = sp.edit();
//                        editor.putInt(LibroInformacion.USER_ID, user.getId());
//                        editor.apply();

                        addToEncriptedSharePreferences(user);

                        // Sale un toast con el mensaje de sesión iniciada y carga el menú principal

                        Toast.makeText(Login.this, "Sesión iniciada", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Login.this, MenuPrincipal.class);
                        startActivity(intent);
                        return;
                    }
                }

                // En caso de que las credenciales sean incorrectas se devolverá un mensaje informativo

                Toast.makeText(Login.this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(Login.this, "Error al buscar los usuarios", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void addToEncriptedSharePreferences(User user) {
        try {
            MasterKey mk = new MasterKey.Builder(this)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            SharedPreferences encryptedSp = EncryptedSharedPreferences.create(this, "ENCRYPTEDSHARE",
                    mk,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);

            SharedPreferences.Editor encryptedEditor = encryptedSp.edit();


            encryptedEditor.putInt(LibroInformacion.USER_ID, user.getId());

            encryptedEditor.apply();


        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}