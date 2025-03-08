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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bibliotecateis.API.models.Book;
import com.example.bibliotecateis.API.repository.BookRepository;
import com.example.bibliotecateis.Helpers;
import com.example.bibliotecateis.R;
import com.example.bibliotecateis.viewModels.BooksViewModel;

import java.util.ArrayList;
import java.util.List;

public class ListadoLibros extends AppCompatActivity {

    private BookRepository bookRepository;
    private RecyclerView recyclerViewLibros;
    private Button btnBuscarAutor, btnBuscarTitulo,btnEliminarFiltro;
    private EditText etBuscar;
    private Toolbar tb;
    private BooksViewModel booksViewModel;
    private BookAdapter adapter;

    // ADICIÓN: un arreglo para almacenar el ISBN escaneado
    private String[] isbnEscaneado = new String[]{""};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listado_biblioteca);

        findIds();

        // Llamamos para cargar los libros inicialmente
        cargarBooks();

        setOCL();


        // ADICIÓN: REGISTRAR el Launcher ANTES de cargar la toolbar
        Helpers.inicializarQRLauncher(this, isbnEscaneado, result -> {
            // Este callback se llama cuando el escaneo finaliza
            isbnEscaneado[0] = result;
            System.out.println("ISBN escaneado desde ListadoLibros: " + isbnEscaneado[0]);

            // 1. Buscamos en la lista de libros
            BookRepository repo = new BookRepository();
            repo.getBooks(new BookRepository.ApiCallback<List<Book>>() {
                @Override
                public void onSuccess(List<Book> books) {
                    // 2. Si coincide, abrimos el detalle
                    for (Book b : books) {
                        if (b.getIsbn().equals(isbnEscaneado[0])) {
                            Intent intent = new Intent(ListadoLibros.this, LibroInformacion.class);
                            intent.putExtra(LibroInformacion.BOOK_ID_EXTRA, b.getId());
                            startActivity(intent);
                            return; // Salimos
                        }
                    }
                    // 3. Si no encontramos nada:
                    Toast.makeText(ListadoLibros.this,
                            "No se encontró el libro con ISBN: " + isbnEscaneado[0],
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Throwable t) {
                    Toast.makeText(ListadoLibros.this,
                            "Error al buscar libros: " + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        });

    }

    private void setOCL() {
        // Escuchadores de los botones de búsqueda
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

        btnEliminarFiltro.setOnClickListener((view) -> {
            cargarBooks();
            etBuscar.setText("");
        });
    }

    private void findIds() {
        btnBuscarAutor   = findViewById(R.id.btnFiltrarAutor);
        btnBuscarTitulo  = findViewById(R.id.btnFiltrarTitulo);
        etBuscar         = findViewById(R.id.etBuscar);
        recyclerViewLibros = findViewById(R.id.recyclerViewLibros);
        recyclerViewLibros.setLayoutManager(new LinearLayoutManager(this));
        btnEliminarFiltro = findViewById(R.id.btnEliminarFiltro);
        tb = findViewById(R.id.toolbar);
        // Finalmente cargamos la toolbar
        cargarToolbar(this, tb);
    }

    private void cargarVM(List<Book> booksSinFiltrar) {
        List<Book> books = Helpers.getLibrosSinRepetir(booksSinFiltrar);




        booksViewModel = new ViewModelProvider(this).get(BooksViewModel.class);
        adapter = new BookAdapter();
        recyclerViewLibros.setAdapter(adapter);
        booksViewModel.getBooks().observe(this, new Observer<List<Book>>() {
            @Override
            public void onChanged(List<Book> books) {
                adapter.setBooks(books);
            }
        });


        booksViewModel.setBooks(books);

    }

    public void cargarBooks() {
        bookRepository = new BookRepository();

        bookRepository.getBooks(new BookRepository.ApiCallback<List<Book>>() {
            @Override
            public void onSuccess(List<Book> result) {
                cargarVM(result);
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(ListadoLibros.this, "Error al buscar los libros", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void buscarAutor(String autor, List<Book> books) {
        cargarVM(books.stream()
                .filter(book -> book.getAuthor().toLowerCase().contains(autor.toLowerCase()))
                .toList());
    }

    public void buscarTitulo(String titulo, List<Book> books){
        cargarVM(books.stream()
                .filter(book -> book.getTitle().toLowerCase().contains(titulo.toLowerCase()))
                .toList());
    }

    public void buscarPrestado(List<Book> books){
        cargarVM(books.stream().filter(book -> !book.isAvailable()).toList());
    }

    private class BookAdapter extends RecyclerView.Adapter<BookAdapter.MyViewHolder> {
        private List<Book> books = new ArrayList<>();

        class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView img1;
            TextView txt1, txt2, txt_disponibles, txt_existencias;
            Button btn1;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                btn1  = itemView.findViewById(R.id.btn1);
                img1  = itemView.findViewById(R.id.img1);
                txt1  = itemView.findViewById(R.id.txt1);
                txt2  = itemView.findViewById(R.id.txt2);
                txt_disponibles = itemView.findViewById(R.id.txt_disponibles);
                txt_existencias = itemView.findViewById(R.id.txt_existencias);
            }
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_tarjeta_book, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            MyViewHolder myvh = (MyViewHolder) holder;
            Book book = books.get(position);

            // Título y autor
            myvh.txt1.setText(book.getTitle());
            myvh.txt2.setText(book.getAuthor());

            // Existencias
            Helpers.obtenerExistencias(book, myvh.txt_existencias, myvh.txt_disponibles,null);

            // Botón que abre la info detallada
            myvh.btn1.setOnClickListener((view) -> {
                Intent intent = new Intent(ListadoLibros.this, LibroInformacion.class);
                intent.putExtra(LibroInformacion.BOOK_ID_EXTRA, book.getId());
                startActivity(intent);
            });

            // Imagen del libro
            String urlImagen = book.getBookPicture();
            if (urlImagen != null && !urlImagen.isEmpty()) {
                Helpers.cargarImagen(urlImagen, myvh.img1);
            } else {
                myvh.img1.setImageResource(R.drawable.portada_libro_default);
            }
        }

        @Override
        public int getItemCount() {
            return books.size();
        }

        void setBooks(List<Book> newBooks) {
            this.books = newBooks;
            notifyDataSetChanged();
        }
    }
}
