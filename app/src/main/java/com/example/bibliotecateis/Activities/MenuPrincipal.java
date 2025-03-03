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
    public static String USER_ID = "userId";
    public static String BOOK_ID_EXTRA = "bookId";
    private BookRepository bookRepository;
    private RecyclerView recyclerMenu;
    private Toolbar tb;
    public int userId = 0;
    public int bookId = 0;
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

        tb = findViewById(R.id.toolbar);

        userId = getSharedPreferences(Login.SHARED_PREFERENCES, MODE_PRIVATE).getInt(USER_ID, 0);
        bookId = getIntent().getIntExtra(BOOK_ID_EXTRA, 0);
        Helpers.inicializarQRLauncher(this, isbnEscaneado, result -> {
            isbnEscaneado[0] = result;
            System.out.println("ISBN escaneado desde MenuPrincipal: " + isbnEscaneado[0]);

            BookRepository repo = new BookRepository();
            repo.getBooks(new BookRepository.ApiCallback<List<Book>>() {
                @Override
                public void onSuccess(List<Book> books) {
                    for (Book book : books) {
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
        Helpers.cargarToolbar(this, tb);
    }

    private void cargarAdapter(List<Book> booksrandom) {

        List<Book> books = Helpers.getLibrosRandom(booksrandom,5);

        recyclerMenu.setAdapter(new RecyclerView.Adapter() {

            class MyViewHolder extends RecyclerView.ViewHolder {
                TextView txtTitulo, txtAutor;
                ImageButton imgBtnLibro;

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

            /*AQUI SE ASIGNA EL FRAGMENT DE LA VISTA DE LIBROS */

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_libro_menu, parent, false);
                return new MyViewHolder(view);
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

                MyViewHolder myvh = (MyViewHolder) holder;
                Book book = books.get(position);
                myvh.getTxtTitulo().setText(book.getTitle());
                myvh.getTxtAutor().setText(book.getAuthor());

                myvh.getImgBtnLibro().setOnClickListener((view) -> {
                    Intent intent = new Intent(MenuPrincipal.this, LibroInformacion.class);
                    intent.putExtra(LibroInformacion.BOOK_ID_EXTRA, book.getId());
                    startActivity(intent);
                });

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