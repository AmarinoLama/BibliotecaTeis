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

    // Variable extraída para que no se repita tanto código
    private BookRepository bookRepository = new BookRepository();

    // Otras variables del xml para poder trabajar con ellas
    private RecyclerView recyclerViewLibros;
    private Button btnBuscarAutor, btnBuscarTitulo,btnEliminarFiltro;
    private EditText etBuscar;
    private Toolbar tb;

    // Variable donde se guarda el isbnEscaneado al leer el QR
    private String[] isbnEscaneado = new String[]{""};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listado_biblioteca);

        // Inicializamos las variables con sus ids
        btnBuscarAutor   = findViewById(R.id.btnFiltrarAutor);
        btnBuscarTitulo  = findViewById(R.id.btnFiltrarTitulo);
        etBuscar         = findViewById(R.id.etBuscar);
        recyclerViewLibros = findViewById(R.id.recyclerViewLibros);
        recyclerViewLibros.setLayoutManager(new LinearLayoutManager(this));
        btnEliminarFiltro = findViewById(R.id.btnEliminarFiltro);
        tb = findViewById(R.id.toolbar);

        // Llamamos para cargar los libros inicialmente
        cargarBooks();

        // Escuchador del botón de búsqueda por Autor
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

        // Escuchador del botón de búsqueda por Título
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

        // Escuchador del botón de eliminar filtro
        btnEliminarFiltro.setOnClickListener((view) -> {
            cargarBooks();
            etBuscar.setText("");
        });

        // Se inicializa el QRLauncher y se le mete toda la lógica para cuando se escanee un QR
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

        // Finalmente cargamos la toolbar
        cargarToolbar(this, tb);
    }

    // Método que carga el adapter con todos los libros existentes en la biblioteca

    private void cargarAdapter(List<Book> booksSinFiltrar) {

        // Se borran los libros repetidos
        List<Book> books = Helpers.getLibrosSinRepetir(booksSinFiltrar);

        // Se settea el adapter con el fragment
        recyclerViewLibros.setAdapter(new RecyclerView.Adapter() {
            class MyViewHolder extends RecyclerView.ViewHolder {

                // Variables que se necesitan para el fragment
                ImageView img1;
                TextView txt1, txt2, txt_disponibles, txt_existencias;
                Button btn1;

                // Constructor del fragment donde se inicializan las variables
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

            // Función que crea el fragment
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_tarjeta_book, parent, false);
                return new MyViewHolder(view);
            }

            // Función que se encarga de llenar el fragment con los datos de los libros
            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
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
        });
    }

    // Método que carga los libros de la biblioteca (todos los existentes sin revisar que se repitan)

    public void cargarBooks() {
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

    // Método que busca los libros por autor
    public void buscarAutor(String autor, List<Book> books) {
        cargarAdapter(books.stream()
                .filter(book -> book.getAuthor().toLowerCase().contains(autor.toLowerCase())) // Funciona con búsqueda parcial gracias al contains
                .toList());
    }

    // Método que busca los libros por título
    public void buscarTitulo(String titulo, List<Book> books){
        cargarAdapter(books.stream()
                .filter(book -> book.getTitle().toLowerCase().contains(titulo.toLowerCase())) // Funciona con búsqueda parcial gracias al contains
                .toList());
    }

    // Método que busca los libros prestados
    // Sin uso pero no se borra por si se necesita en un futuro
    public void buscarPrestado(List<Book> books){
        cargarAdapter(books.stream().filter(book -> !book.isAvailable()).toList());
    }
}
