package com.example.mariangeles.practica6aad.Datos;


import android.net.Uri;
import android.provider.BaseColumns;


public abstract class TablaInmueble implements BaseColumns {
    public static final String TABLA ="Inmueble";
    public static final String SUBIDO ="Subido";
    public static final String LOCALIDAD = "Localidad";
    public static final String CALLE = "Calle";
    public static final String NUMERO = "Numero";
    public static final String TIPO = "Tipo";
    public static final String PRECIO = "Precio";
    public static final String IMAGENES = "Imagenes";

    public static final String CONTENT_TYPE =
            "vnd.android.cursor.dir/vnd.izv.inmuebles";
    public static final Uri CONTENT_URI =Uri.parse("content://" +
            Proveedor.AUTORIDAD + "/" + TABLA);
    public static final String[] COLUMNAS={ _ID, SUBIDO, LOCALIDAD,
            CALLE, NUMERO, TIPO, PRECIO, IMAGENES };

}
