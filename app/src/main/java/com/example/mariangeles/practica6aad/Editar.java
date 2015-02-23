package com.example.mariangeles.practica6aad;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.mariangeles.practica6aad.Datos.TablaInmueble;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


public class Editar extends Activity {
    private int i;
    private EditText localidad, calle, numero, tipo, precio;
    private ImageView iv;
    private String nombreImg="sin imagen";
    private Cursor b;
    private boolean captura=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edicion);
        initComponents();

        Bundle b = getIntent().getExtras();
        if(b!= null) {
            i=b.getInt("index");//si i es -1 no hay que editar
            if(i!= -1){
                mostrar();
            }
        }
    }

    private void initComponents(){
        localidad=(EditText)findViewById(R.id.localidad);
        calle=(EditText)findViewById(R.id.calle);
        numero=(EditText)findViewById(R.id.numero);
        tipo=(EditText)findViewById(R.id.tipo);
        precio=(EditText)findViewById(R.id.precio);
        iv=(ImageView)findViewById(R.id.mostrarFoto);
    }

    public void mostrar(){
        //query(Uri uri, String[] proy, String cond, String[] param, String ord)

        b = getContentResolver().query(TablaInmueble.CONTENT_URI,
                TablaInmueble.COLUMNAS,
                TablaInmueble._ID + " = ?",
                new String[] { String.valueOf(i) },
                null);
        b.moveToFirst();

        //mostramos
        localidad.setText(b.getString(2));
        calle.setText(b.getString(3));
        numero.setText(b.getString(4));
        tipo.setText(b.getString(5));
        precio.setText(b.getString(6));

        if (captura) {
            mostrarFoto(nombreImg);
        } else{
            mostrarFoto(b.getString(7));
        }
    }

    //Añadir
    public void aceptar(View v){

        ContentValues valores= new ContentValues();
        valores.put("subido", 0);
        valores.put("localidad", localidad.getText().toString());
        valores.put("calle", calle.getText().toString());
        valores.put("numero", numero.getText().toString());
        valores.put("tipo", tipo.getText().toString());
        valores.put("precio", precio.getText().toString());
        valores.put("imagenes",nombreImg);

        if(i!=-1) {
            //update(Uri uri, ContentValues valores, String cond, String[] param)
            getContentResolver().update(TablaInmueble.CONTENT_URI,
                    valores,
                    TablaInmueble._ID + " = ?",
                    new String[]{String.valueOf(i)});
        }else {
            //insert(Uri uri, ContentValues valores)
            getContentResolver().insert(TablaInmueble.CONTENT_URI,
                    valores);
        }
        finish();
    }

    public void cancelar(View v){
        finish();
    }

    public void tostada(String s){
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }


    /****** Foto *******/
    public void foto(View v){
        captura=true;
        Intent intent = new Intent (MediaStore.ACTION_IMAGE_CAPTURE);
        nombreImg = "inmueble_"+getDatePhone()+".jpg";
        File f = new File(getExternalFilesDir(null), nombreImg);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        startActivity(intent);
    }

    private String getDatePhone() {
        Calendar cal = new GregorianCalendar();
        Date date = cal.getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
        String formateDate = df.format(date);
        return formateDate;
    }

    private void mostrarFoto(String nombre){
        File imgFile = new File(getExternalFilesDir(null), nombre);
        if(imgFile.exists()){
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            iv.setImageBitmap(myBitmap);
        }
    }
}
