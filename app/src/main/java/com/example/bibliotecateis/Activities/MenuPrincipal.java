package com.example.bibliotecateis.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bibliotecateis.API.models.Book;
import com.example.bibliotecateis.API.repository.BookRepository;
import com.example.bibliotecateis.Helpers;
import com.example.bibliotecateis.Login.Login;
import com.example.bibliotecateis.R;
import java.util.List;

public class MenuPrincipal extends AppCompatActivity {

    // Variables para gestionar la información que se pasa entre intents
    public static String USER_ID = "userId";
    public static String BOOK_ID_EXTRA = "bookId";

    // Varible para que no se repita tanto el código
    private BookRepository bookRepository;

    // Variables del xml para poder trabajar con ellas
    private RecyclerView recyclerMenu;
    private Toolbar tb;

    // Variables para guardar el userId y el bookId
    public int userId = 0;
    public int bookId = 0;

    // Variable donde se guarda el isbnEscaneado al leer el QR
    private String[] isbnEscaneado = new String[]{""};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.menu_principal);

        recyclerMenu = findViewById(R.id.recyclerMenu);
        //Esto sería para que el recyclerview fuera en vertical
        //recyclerMenu.setLayoutManager(new LinearLayoutManager((this)));
        recyclerMenu.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        cargarBooks();

        // Obtenemos el userId y el bookId

        userId = getSharedPreferences(Login.SHARED_PREFERENCES, MODE_PRIVATE).getInt(USER_ID, 0);

        bookId = getIntent().getIntExtra(BOOK_ID_EXTRA, 0);

        // Se inicializa el QRLauncher y se le mete toda la lógica para cuando se escanee un QR
        Helpers.inicializarQRLauncher(this, isbnEscaneado, result -> {
            isbnEscaneado[0] = result;
            System.out.println("ISBN escaneado desde MenuPrincipal: " + isbnEscaneado[0]);

            // Cuando encuentra el libro se abre la vista detallada de este
            // Hemos intenado extraer este método pero no hemos sido capaces
            BookRepository repo = new BookRepository();
            repo.getBooks(new BookRepository.ApiCallback<List<Book>>() {
                @Override
                public void onSuccess(List<Book> books) {
                    for (Book book : books) {
                        // Si coincide el isbn se abre el intent de LibroInformacion con la información del libro
                        if (book.getIsbn().equals(isbnEscaneado[0])) {
                            Intent intent = new Intent(MenuPrincipal.this, LibroInformacion.class);
                            intent.putExtra(LibroInformacion.BOOK_ID_EXTRA, book.getId());
                            startActivity(intent);
                            return;
                        }
                    }
                    Toast.makeText(MenuPrincipal.this, "No se encontró el libro con ese ISBN", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Throwable t) {
                    Toast.makeText(MenuPrincipal.this,
                            "Error al buscar libros: " + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Inicializamos el toolbar
        tb = findViewById(R.id.toolbar);
        Helpers.cargarToolbar(this, tb);
    }

    // Cargamos el adapter con los libros
    private void cargarAdapter(List<Book> booksrandom) {

        // Con el método getLibrosRandom obtenemos 5 libros aleatorios
        List<Book> books = Helpers.getLibrosRandom(booksrandom,5);

        // Creamos el adapter
        recyclerMenu.setAdapter(new RecyclerView.Adapter() {
            class MyViewHolder extends RecyclerView.ViewHolder {

                // Variables para poder trabajar con los elementos del xml
                TextView txtTitulo, txtAutor;
                ImageButton imgBtnLibro;

                // Constructor donde se inicializan las variables
                public MyViewHolder(@NonNull View itemView) {
                    super(itemView);
                    imgBtnLibro = itemView.findViewById(R.id.imgBtnLibro);
                    txtTitulo = itemView.findViewById(R.id.txtTitulo);
                    txtAutor = itemView.findViewById(R.id.txtAutor);
                }

                public ImageButton getImgBtnLibro() {
                    return imgBtnLibro;
                }

                public TextView getTxtTitulo() {
                    return txtTitulo;
                }

                public TextView getTxtAutor() {
                    return txtAutor;
                }
            }

            // AQUÍ SE ASIGNA EL FRAGMENT DE LA VISTA DE LIBROS

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_libro_menu, parent, false);
                return new MyViewHolder(view);
            }

            // Método que se encarga de asignar los valores a los elementos del fragment
            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

                // Se asignan los valores a los elementos del fragment
                MyViewHolder myvh = (MyViewHolder) holder;
                Book book = books.get(position);
                myvh.getTxtTitulo().setText(book.getTitle());
                myvh.getTxtAutor().setText(book.getAuthor());

                // Se asigna la imagen al botón si le das click te lleva a la información detallada del libro
                myvh.getImgBtnLibro().setOnClickListener((view) -> {
                    Intent intent = new Intent(MenuPrincipal.this, LibroInformacion.class);
                    intent.putExtra(LibroInformacion.BOOK_ID_EXTRA, book.getId());
                    startActivity(intent);
                });

                // Se carga la imagen del libro
                String urlImagen = book.getBookPicture();
                if (urlImagen != null && !urlImagen.isEmpty()) {
                    Helpers.cargarImagen(urlImagen, myvh.getImgBtnLibro());
                } else {
                    myvh.getImgBtnLibro().setImageResource(R.drawable.portada_libro_default);
                }
            }

            @Override
            public int getItemCount() {
                return books.size();
            }
        });
    }

    // Método para cargar los libros
    public void cargarBooks() {
        bookRepository = new BookRepository();
        bookRepository.getBooks(new BookRepository.ApiCallback<List<Book>>() {
            @Override
            public void onSuccess(List<Book> result) {
                cargarAdapter(result);
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(MenuPrincipal.this, "Error al buscar los libros", Toast.LENGTH_SHORT).show();
            }
        });
    }
}