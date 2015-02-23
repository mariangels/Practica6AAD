package com.example.mariangeles.practica6aad.Datos;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;


public class Proveedor extends ContentProvider{

    private Ayudante ay;
    static String AUTORIDAD = "com.example.mariangeles.practica4aad.Datos.Proveedor";
    private static  UriMatcher convierteUri2Int;
    private static final int INMUEBLES = 1;
    private static final int INMUEBLE_ID = 2;

    @Override
    public Cursor query(Uri uri, String[] proy, String cond, String[] param, String ord) {

        //select proy from TABLA where condicion order by ord
            SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
            qb.setTables(TablaInmueble.TABLA);
            switch (convierteUri2Int.match(uri)) {
                case INMUEBLES: break;
                case INMUEBLE_ID: cond = cond + "_id = " + uri.getLastPathSegment();
                    break;
                default:
                    throw new IllegalArgumentException("URI " + uri);
            }
            SQLiteDatabase db = ay.getReadableDatabase();
            Cursor c = qb.query(db, proy, cond, param, null, null, ord);
            c.setNotificationUri(getContext().getContentResolver(), uri);
            return c;
    }

    static {
        convierteUri2Int = new UriMatcher(UriMatcher.NO_MATCH);
        convierteUri2Int.addURI(AUTORIDAD,TablaInmueble.TABLA, INMUEBLES);
        convierteUri2Int.addURI(AUTORIDAD,TablaInmueble.TABLA + "/#", INMUEBLE_ID);
    }


    @Override
    public String getType(Uri uri) {
        switch (convierteUri2Int.match(uri)) {
            case INMUEBLES:
                return TablaInmueble.CONTENT_TYPE;
            default:
                throw new IllegalArgumentException("URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues valores) {
        if (convierteUri2Int.match(uri) != INMUEBLES) {
            throw new IllegalArgumentException("URI " + uri);
        }
        SQLiteDatabase db = ay.getWritableDatabase();
        long id = db.insert(TablaInmueble.TABLA, null, valores);
        if (id > 0) {
            Uri uriElemento = ContentUris.withAppendedId(TablaInmueble.CONTENT_URI, id);
            getContext().getContentResolver().notifyChange(uriElemento, null);
            return uriElemento;
        }
        throw new SQLException("Insert" + uri);
    }

    @Override
    public int delete(Uri uri, String cond, String[] param) {
        SQLiteDatabase db = ay.getWritableDatabase();
        switch (convierteUri2Int.match(uri)) {
            case INMUEBLES: break;
            case INMUEBLE_ID: cond = cond + "_id = " + uri.getLastPathSegment();
                break;
            default: throw new IllegalArgumentException("URI " + uri);
        }
        int cuenta = db.delete(TablaInmueble.TABLA, cond, param);
        getContext().getContentResolver().notifyChange(uri, null);
        return cuenta;

    }

    @Override
    public int update(Uri uri, ContentValues valores, String cond, String[] param) {
        SQLiteDatabase db = ay.getWritableDatabase();
        int cuenta;
        switch (convierteUri2Int.match(uri)) {
            case INMUEBLES: cuenta = db.update(
                    TablaInmueble.TABLA, valores, cond, param);
                break;
            default: throw new IllegalArgumentException("URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return cuenta;
    }

    @Override
    public boolean onCreate() {
        ay = new Ayudante(getContext());
        return true;
    }
}
