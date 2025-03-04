package com.example.bibliotecateis.Activities;

import static com.example.bibliotecateis.Activities.LibroInformacion.USER_ID;
import androidx.appcompat.widget.Toolbar;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bibliotecateis.API.models.Book;
import com.example.bibliotecateis.API.models.BookLending;
import com.example.bibliotecateis.API.repository.BookLendingRepository;
import com.example.bibliotecateis.API.repository.BookRepository;
import com.example.bibliotecateis.Helpers;
import com.example.bibliotecateis.Login.Login;
import com.example.bibliotecateis.R;
import java.util.Comparator;
import java.util.List;

public class LibrosUsuario extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Toolbar tb;

    // Variable que se consigue con el shared preferences
    private int userId = 0;

    // Variable donde se guardará lo que obtenga al leer el QR
    private String[] isbnEscaneado = new String[]{""};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_libros_usuario);

        // Obtenemos el id del usuario mediante las shared preferences
        userId = getSharedPreferences(Login.SHARED_PREFERENCES, MODE_PRIVATE).getInt(USER_ID, 0);

        // Se inicializa el QRLauncher y se le mete toda la lógica para cuando se escanee un QR
        Helpers.inicializarQRLauncher(this, isbnEscaneado, result -> {
            isbnEscaneado[0] = result;
            System.out.println("QR Escaneado desde LibrosUsuario: " + isbnEscaneado[0]);

            // Cuando encuentra el libro se abre la vista detallada de este
            // Hemos intenado extraer este método pero no hemos sido capaces
            BookRepository bookRepository = new BookRepository();
            bookRepository.getBooks(new BookRepository.ApiCallback<List<Book>>() {
                @Override
                public void onSuccess(List<Book> books) {
                    for (Book b : books) {
                        // Si coincide el isbn se abre el intent de LibroInformacion con la información del libro
                        if (b.getIsbn().equals(isbnEscaneado[0])) {
                            Intent i = new Intent(LibrosUsuario.this, LibroInformacion.class);
                            i.putExtra(LibroInformacion.BOOK_ID_EXTRA, b.getId());
                            startActivity(i);
                            return;
                        }
                    }
                    Toast.makeText(LibrosUsuario.this,
                            "No se encontró el libro con ISBN: " + isbnEscaneado[0],
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Throwable t) {
                    Toast.makeText(LibrosUsuario.this,
                            "Error al cargar los libros: " + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Cargamos el toolbar y sus opciones
        tb = findViewById(R.id.toolbar);
        Helpers.cargarToolbar(this, tb);

        // Cargamos el recycler view con los libros del usuario
        recyclerView = findViewById(R.id.recyclerLibrosUsuario);
        recyclerView.setLayoutManager(new LinearLayoutManager((this)));
        cargarBooks();
    }

    // Método que carga el adapter con los libros del usuario, esto se encarga de mostrar todos las existencias de libros que tiene el usuario, con un fragment individual para cada libro

    private void cargarAdapter(List<BookLending> userBooks) {

        // Se obtienen primero todos los libros de ese usuario
        List<Book> books = Helpers.getUserBooksFromLendings(userBooks, userId);
        System.out.println("Books: " + books.size());

        // Se settea el adapter con el fragment
        recyclerView.setAdapter(new RecyclerView.Adapter() {

            class MyViewHolder extends RecyclerView.ViewHolder {

                // Variables que se necesitan para el fragment
                TextView txtTitulo, txtAutor, txtFechaDevolucion, txtIsbn;
                Button btnDevolver;
                ImageView imgPortada;

                // Constructor para inicializar las variables
                public MyViewHolder(@NonNull View itemView) {
                    super(itemView);
                    txtTitulo = itemView.findViewById(R.id.txtTitulo);
                    txtAutor = itemView.findViewById(R.id.txtAutor);
                    txtFechaDevolucion = itemView.findViewById(R.id.txtFechaDevolucion);
                    txtIsbn = itemView.findViewById(R.id.txtIsbn);
                    btnDevolver = itemView.findViewById(R.id.btnDevolver);
                    imgPortada = itemView.findViewById(R.id.imgPortada);
                }

                public TextView getTxtTitulo() {
                    return txtTitulo;
                }

                public TextView getTxtAutor() {
                    return txtAutor;
                }

                public TextView getTxtFechaDevolucion() {
                    return txtFechaDevolucion;
                }

                public TextView getTxtIsbn() {
                    return txtIsbn;
                }

                public Button getBtnDevolver() {
                    return btnDevolver;
                }

                public ImageView getImgPortada() {
                    return imgPortada;
                }
            }

            // Función encargada de crear el fragment para cada libro
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_libro_usuario, parent, false);
                return new MyViewHolder(view);
            }

            // Función encargada de cargar todos los valores para cada fragment (en este método es el que tocamos todos los libros para settearlos)
            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

                // Se obtiene el viewHolder, es decir, la copia del libro que se va a mostrar
                MyViewHolder myvh = (MyViewHolder) holder;
                // Se obtiene el libro
                Book book = books.get(position);

                // Se setean las variables del fragment con los valores del libro
                myvh.getTxtTitulo().setText(book.getTitle());
                myvh.getTxtAutor().setText(book.getAuthor());
                myvh.getTxtIsbn().setText(book.getIsbn());

                // Se obtiene la fecha de devolución del libro
                Helpers.getNextDevolucionUsuario(book, userId, myvh.getTxtFechaDevolucion());

                // Botón con un listener para devolver el libro y recargar la página para que se actualice
                myvh.getBtnDevolver().setOnClickListener((view) -> {
                    Helpers.devolverLibro(book.getId());
                    Intent i = new Intent(LibrosUsuario.this, LibrosUsuario.class);
                    startActivity(i);
                });

                // Parte que se encarga de cargar la imagen del libro, en caso de que tenga se le settea la suya y si no tiene se le adjunta una default
                String urlImagen = book.getBookPicture();
                if (urlImagen != null && !urlImagen.isEmpty()) {
                    Helpers.cargarImagen(urlImagen, myvh.getImgPortada());
                } else {
                    myvh.getImgPortada().setImageResource(R.drawable.portada_libro_default);
                }
            }

            @Override
            public int getItemCount() {
                return books.size();
            }
        });
    }

    // Función que se encarga de hacerle la petición a la API para obtener todos los libros y cargarlos en el adapter
    public void cargarBooks() {
        BookLendingRepository bookLendingRepository = new BookLendingRepository();
        bookLendingRepository.getAllLendings(new BookRepository.ApiCallback<List<BookLending>>() {
            @Override
            public void onSuccess(List<BookLending> result) {
                result = result.stream()
                        .filter(b -> b.getUserId() == userId && b.getReturnDate() == null) // cojo solo los libros del usuario que no hayan sido devueltos
                        .sorted(Comparator.comparing(BookLending::getLendDate)).toList();
                cargarAdapter(result);
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(LibrosUsuario.this, "Error al cargar los libros", Toast.LENGTH_SHORT).show();
            }
        });
    }
}