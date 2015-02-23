package com.example.mariangeles.practica6aad;


import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mariangeles.practica6aad.Datos.TablaInmueble;

import java.io.File;


public class FragmentoDetalle extends Fragment {

    private TextView tvTitulo, tvContenido;
    private ImageView imagen;
    private int i;

    public FragmentoDetalle() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragmento_detalle, container, false);
        tvTitulo  = (TextView) v.findViewById(R.id.textView);
        tvContenido = (TextView) v.findViewById(R.id.textView2);
        imagen = (ImageView) v.findViewById(R.id.imagen);

        Bundle b = getActivity().getIntent().getExtras();
        if(b!= null) {
            i = b.getInt("index");
            mostrar(i);
        }else{
            tvTitulo.setText("");
            tvContenido.setText("");
        }
        return v;
    }

    public void mostrar(int id) {

        Cursor c = getActivity().getContentResolver()
                .query(TablaInmueble.CONTENT_URI, TablaInmueble.COLUMNAS,
                        TablaInmueble._ID + " = ?", new String[]{String.valueOf(id)}, null);
        c.moveToFirst();
        tvTitulo.setText(" ID: " + id);
        tvContenido.setText(
                " LOCALIDAD:  " + c.getString(2) + "\n" +
                " CALLE:  " + c.getString(3) + "\n" +
                " NUMERO:  " + c.getString(4) + "\n" +
                " TIPO:  " + c.getString(5) + "\n" +
                " PRECIO:  " + c.getString(6)+ "\n" );

        try{
            mostrarFoto(c.getString(7));
        }catch(NullPointerException e){
            imagen.setImageResource( R.drawable.inmobiliaria);
        }
    }

    private void mostrarFoto(String nombre){
        File imgFile = new File(getActivity().getExternalFilesDir(null), nombre);
        if (imgFile.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            imagen.setImageResource(0);
            imagen.setImageBitmap(myBitmap);
        }else{
            imagen.setImageResource( R.drawable.inmobiliaria);
        }
    }
}
