package com.example.bibliotecateis.Activities;

import static com.example.bibliotecateis.Helpers.cargarToolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bibliotecateis.API.models.Book;
import com.example.bibliotecateis.API.repository.BookRepository;
import com.example.bibliotecateis.Helpers;
import com.example.bibliotecateis.R;

import java.util.List;

public class ListadoLibros extends AppCompatActivity {

    private BookRepository bookRepository;
    private RecyclerView recyclerViewLibros;
    private Button btnBuscarAutor, btnBuscarTitulo;
    private EditText etBuscar;
    private Toolbar tb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listado_biblioteca);
        btnBuscarAutor = findViewById(R.id.btnFiltrarAutor);
        btnBuscarTitulo = findViewById(R.id.btnFiltrarTitulo);
        etBuscar = findViewById(R.id.etBuscar);

        recyclerViewLibros = findViewById(R.id.recyclerViewLibros);
        recyclerViewLibros.setLayoutManager(new LinearLayoutManager((this)));
        cargarBooks();

        btnBuscarAutor.setOnClickListener((view) -> {
            bookRepository.getBooks(new BookRepository.ApiCallback<List<Book>>() {
                @Override
                public void onSuccess(List<Book> result) {
                    buscarAutor(etBuscar.getText().toString(), result);
                }
                @Override
                public void onFailure(Throwable t) {
                    Toast.makeText(ListadoLibros.this, "Error al buscar los libros", Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnBuscarTitulo.setOnClickListener((view) -> {
            bookRepository.getBooks(new BookRepository.ApiCallback<List<Book>>() {
                @Override
                public void onSuccess(List<Book> result) {
                    buscarTitulo(etBuscar.getText().toString(), result);
                }
                @Override
                public void onFailure(Throwable t) {
                    Toast.makeText(ListadoLibros.this, "Error al buscar los libros", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // uso esto porque todas las toolbar van a ser iguales, asi que le paso el contexto y toda la logica que voy a repetir en todas las clases las manejo en helpers
        tb = findViewById(R.id.toolbar);
        cargarToolbar(this,tb);
    }

    private void cargarAdapter(List<Book> booksSinFiltrar) {

        List<Book> books = Helpers.getLibrosSinRepetir(booksSinFiltrar);

        recyclerViewLibros.setAdapter(new RecyclerView.Adapter() {

            class MyViewHolder extends RecyclerView.ViewHolder {
                ImageView img1;
                TextView txt1, txt2, txt_disponibles, txt_existencias;
                Button btn1;

                public MyViewHolder(@NonNull View itemView) {
                    super(itemView);
                    btn1 = itemView.findViewById(R.id.btn1);
                    img1 = itemView.findViewById(R.id.img1);
                    txt1 = itemView.findViewById(R.id.txt1);
                    txt2 = itemView.findViewById(R.id.txt2);
                    txt_disponibles = itemView.findViewById(R.id.txt_disponibles);
                    txt_existencias = itemView.findViewById(R.id.txt_existencias);
                }

                public ImageView getImg1() {
                    return img1;
                }

                public TextView getTxt1() {
                    return txt1;
                }

                public Button getBtn1() {
                    return btn1;
                }

                public TextView getTxt2() {
                    return txt2;
                }
            }

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_tarjeta_book, parent, false);
                return new MyViewHolder(view);
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                MyViewHolder myvh = (MyViewHolder) holder;
                Book book = books.get(position);
                myvh.getTxt1().setText(book.getTitle());
                myvh.getTxt2().setText(book.getAuthor());

                Helpers.obtenerExistencias(book, myvh.txt_existencias, myvh.txt_disponibles);

                myvh.getBtn1().setOnClickListener((view) -> {
                    Intent intent = new Intent(ListadoLibros.this, LibroInformacion.class);
                    intent.putExtra(LibroInformacion.BOOK_ID_EXTRA, book.getId());
                    startActivity(intent);
                });
                String urlImagen = book.getBookPicture();
                if (urlImagen != null && !urlImagen.isEmpty()) {
                    Helpers.cargarImagen(urlImagen, myvh.getImg1());
                } else {
                    myvh.getImg1().setImageResource(R.drawable.portada_libro_default);
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
                Toast.makeText(ListadoLibros.this, "Error al buscar los libros", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void buscarAutor(String autor, List<Book> books) {
        cargarAdapter(books.stream()
                .filter(book -> book.getAuthor().toLowerCase().contains(autor.toLowerCase()))
                .toList());
    }

    public void buscarTitulo(String titulo, List<Book> books){
        cargarAdapter(books.stream()
                .filter(book -> book.getTitle().toLowerCase().contains(titulo.toLowerCase()))
                .toList());
    }

    public void buscarPrestado(List<Book> books){
        cargarAdapter(books.stream().filter(book -> !book.isAvailable()).toList());
    }
}