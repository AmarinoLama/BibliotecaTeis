package com.example.bibliotecateis;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.bibliotecateis.API.repository.BookRepository;
import com.example.bibliotecateis.API.repository.ImageRepository;

public class Helpers {

    // ESTA CLASE ME DIJO EL PROFE QUE LA CREE, NO TE ENFADES VICTOR <3

    public static void cargarImagen(String bookPicture, ImageView portada) {
        ImageRepository imageRepository = new ImageRepository();
        imageRepository.getImage(bookPicture, new BookRepository.ApiCallback<byte[]>() {
            @Override
            public void onSuccess(byte[] result) {
                if (result != null && result.length > 0) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(result, 0, result.length);
                    portada.setImageBitmap(bitmap);
                }
            }
            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(portada.getContext(), "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
