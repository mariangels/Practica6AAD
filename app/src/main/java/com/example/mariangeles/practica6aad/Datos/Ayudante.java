package com.example.mariangeles.practica6aad.Datos;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class Ayudante extends SQLiteOpenHelper{

    public static final String DATABASE_NAME = "inmobiliaria.sqlite";
    public static final int DATABASE_VERSION = 6;

    public Ayudante(Context context) {
        super(context, context.getExternalFilesDir(null)+"/"+DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql;

        sql = "create table "+ TablaInmueble.TABLA + "( "+
        TablaInmueble._ID +" integer primary key autoincrement, "+
        TablaInmueble.SUBIDO + " integer default 0, "+
        TablaInmueble.LOCALIDAD + " text, "+
        TablaInmueble.CALLE + " text, "+
        TablaInmueble.NUMERO + " text, "+
        TablaInmueble.TIPO + " text, "+
        TablaInmueble.PRECIO + " text, "+
        TablaInmueble.IMAGENES + " text "+
        ")";

        db.execSQL(sql);
        Log.e("sql", sql);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int versionAntigua, int nuevaVersion) {
        String sql;
        sql=" drop table if exists "+ TablaInmueble.TABLA;
        db.execSQL(sql);
        onCreate(db);

    }

}
