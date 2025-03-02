package com.example.bibliotecateis.Activities;

import static com.example.bibliotecateis.Activities.LibroInformacion.USER_ID;
import androidx.appcompat.widget.Toolbar;
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
import java.util.List;

public class LibrosUsuario extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Toolbar tb;

    private BookLendingRepository bookLendingRepository = new BookLendingRepository();
    private int userId = 0;
    private String[] isbnEscaneado = new String[]{""};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_libros_usuario);

        userId = getSharedPreferences(Login.SHARED_PREFERENCES, MODE_PRIVATE).getInt(USER_ID, 0);

        tb = findViewById(R.id.toolbar);
        Helpers.inicializarQRLauncher(this, isbnEscaneado, result -> {
            isbnEscaneado[0] = result;
            System.out.println("QR Escaneado desde LibrosUsuario: " + isbnEscaneado[0]);
        });
        Helpers.cargarToolbar(this, tb);

        recyclerView = findViewById(R.id.recyclerLibrosUsuario);
        recyclerView.setLayoutManager(new LinearLayoutManager((this)));
        cargarBooks();
    }

    private void cargarAdapter(List<BookLending> userBooks) {

        List<Book> books = Helpers.getUserBooksFromLendings(userBooks, userId);
        System.out.println("Books: " + books.size());

        recyclerView.setAdapter(new RecyclerView.Adapter() {

            class MyViewHolder extends RecyclerView.ViewHolder {

                TextView txtTitulo, txtAutor, txtFechaDevolucion, txtIsbn;
                Button btnDevolver;
                ImageView imgPortada;

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

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_libro_usuario, parent, false);
                return new MyViewHolder(view);
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

                MyViewHolder myvh = (MyViewHolder) holder;
                Book book = books.get(position);
                myvh.getTxtTitulo().setText(book.getTitle());

                myvh.getBtnDevolver().setOnClickListener((view) -> {
                    Helpers.devolverLibro(book.getId());
                });

                String urlImagen = book.getBookPicture();
                if (urlImagen != null && !urlImagen.isEmpty()) {
                    Helpers.cargarImagen(urlImagen, myvh.getImgPortada());
                } else {
                    myvh.getImgPortada().setImageResource(R.drawable.portada_libro_default);
                }

                myvh.getTxtAutor().setText(book.getAuthor());
                myvh.getTxtIsbn().setText(book.getIsbn());
                Helpers.getNextDevolucion(book, myvh.getTxtFechaDevolucion());
            }

            @Override
            public int getItemCount() {
                return books.size();
            }
        });
    }

    public void cargarBooks() {
        bookLendingRepository.getAllLendings(new BookRepository.ApiCallback<List<BookLending>>() {
            @Override
            public void onSuccess(List<BookLending> result) {
                cargarAdapter(result);
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(LibrosUsuario.this, "Error al cargar los libros", Toast.LENGTH_SHORT).show();
            }
        });
    }
}