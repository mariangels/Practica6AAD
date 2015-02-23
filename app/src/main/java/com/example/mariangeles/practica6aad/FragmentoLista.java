package com.example.mariangeles.practica6aad;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.mariangeles.practica6aad.Datos.TablaInmueble;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class FragmentoLista extends Fragment {

    private Adaptador ad;
    private ListView lvLista;
    private Cursor c;

    public FragmentoLista( ) {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        View v= inflater.inflate(R.layout.fragmento_lista, container, false);
        lvLista = (ListView) v.findViewById(R.id.lvLista);

        registerForContextMenu(lvLista);

        lvLista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mCallbacks.onItemSelected(((int) l));
            }
        });

        return v;
    }

    @Override
    public void onActivityCreated(Bundle e) {
        super.onActivityCreated(e);
        c = getActivity().getContentResolver().query(TablaInmueble.CONTENT_URI,
                TablaInmueble.COLUMNAS,
                null, null, null);
        ad=new Adaptador(getActivity(), c);
        lvLista.setAdapter(ad);
    }

        /********  LongClick  *********/

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.menu_long_click, menu);
    }

    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info= (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int id=item.getItemId();
        /*
        c.moveToPosition(info.position);
        int idBaseDatos= c.getInt(0);
        */
        Cursor r= (Cursor)lvLista.getItemAtPosition(info.position);
        int idBaseDatos=r.getInt(0);

        switch (id){
            case R.id.borrar:
                act_borrar(idBaseDatos);
                break;
            case R.id.editar:
                act_editar(idBaseDatos);
                break;
            case R.id.subir:
                //Subir
                //...
                c = getActivity().getContentResolver().query(TablaInmueble.CONTENT_URI,
                        TablaInmueble.COLUMNAS,
                        TablaInmueble._ID + " = ?",
                        new String[] { String.valueOf(idBaseDatos) },
                        null);
                c.moveToFirst();
                if (c.getInt(1) == 0) {
                    //no esta subido
                    new SubirUNO().execute();
                }else {
                    tostada("Ya esta subido");
                }
                break;
        }
        return super.onContextItemSelected(item);
    }

    class SubirUNO extends AsyncTask<String,Integer,String> {

        @Override
        protected String doInBackground(String... strings) {

            String url = "?target=prueba" + "&op=insert" + "&action=op";
            //se sube
            String r =post(c, ActividadLista.BASE + url);

            //si tiene imagen
            if(!c.getString(7).equalsIgnoreCase("sin imagen")) {
                File imgFile = new File(getActivity().getExternalFilesDir(null), c.getString(7));
                String a = "http://192.168.1.102:52023/Practica6AAD2/subirimagen";
                postFile(a, "archivo", imgFile);
            }
            return r;
        }

        @Override
        protected void onPostExecute(String r) {
            super.onPostExecute(r);
            tostada(r);
            if(r.equals("{\"r\":1)}")) {
                //cambiar subido a 1
                marcarSubido(c);
                tostada("Subido correctamente");
            }else
                tostada("No se ha podido subir");
        }

        public String post(Cursor c,String urlPeticion) {
            String resultado="";
            try {
                URL url = new URL(urlPeticion);
                HttpURLConnection conexion = (HttpURLConnection) url.openConnection();
                conexion.setDoOutput(true);
                conexion.setRequestMethod("POST");
                MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.STRICT);

                multipartEntity.addPart("usuario", new StringBody(getUsuarioSharedPreferences()));
                multipartEntity.addPart("localidad", new StringBody(c.getString(2)));
                multipartEntity.addPart("calle", new StringBody(c.getString(3)));
                multipartEntity.addPart("numero", new StringBody(c.getString(4)));
                multipartEntity.addPart("tipo", new StringBody(c.getString(5)));
                multipartEntity.addPart("precio", new StringBody(c.getString(6)));
                if(c.getString(7).equalsIgnoreCase("sin imagen")){
                    multipartEntity.addPart("imagen", new StringBody("no hay imagen"));
                }else{
                    multipartEntity.addPart("imagen", new StringBody(c.getString(7)));
                }
                multipartEntity.addPart("nombre", new StringBody("valor"));
                conexion.setRequestProperty("Content-Type", multipartEntity.getContentType().getValue());
                OutputStream out = conexion.getOutputStream();

                try {
                    multipartEntity.writeTo(out);
                } catch (Exception e){
                    Log.v("primero", e.toString());
                    return e.toString();
                }finally {
                    out.close();
                }
                BufferedReader in = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
                String decodedString;

                while ((decodedString = in.readLine()) != null) {
                    resultado+=decodedString;
                }
                in.close();
            } catch (MalformedURLException ex) {
                Log.v("segundo",ex.toString());
                return null;
            } catch (IOException ex) {
                Log.v("tercero",ex.toString());
                return null;
            }
            return resultado;
        }

        public String postFile(String urlPeticion, String nombreParametro, File imgFile) {
            //urlPeticion es la URL de envío
            //nombreParametro es el name del input del html
            //nombreArchivo es el uri.getPath()
            String resultado="";
            int status=0;
            try {
                URL url = new URL(urlPeticion);
                HttpURLConnection conexion = (HttpURLConnection) url.openConnection();
                conexion.setDoOutput(true);
                conexion.setRequestMethod("POST");
                FileBody fileBody = new FileBody(imgFile);
                MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.STRICT);
                multipartEntity.addPart(nombreParametro, fileBody);
                multipartEntity.addPart("nombre", new StringBody("valor"));
                conexion.setRequestProperty("Content-Type", multipartEntity.getContentType().getValue());
                OutputStream out = conexion.getOutputStream();
                try {
                    multipartEntity.writeTo(out);
                } catch (Exception e){
                    Log.v("primero", e.toString());
                    return e.toString();
                }finally {
                    out.close();
                }
                BufferedReader in = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
                String decodedString;
                while ((decodedString = in.readLine()) != null) {
                    resultado+=decodedString+"\n";
                }
                in.close();
                status = conexion.getResponseCode();
            } catch (MalformedURLException ex) {
                Log.v("segundo",ex.toString());
                return null;
            } catch (IOException ex) {
                Log.v("tercero",ex.toString());
                return null;
            }
            return resultado+"\n"+status;
        }

        public void marcarSubido(Cursor cursor){
            if (cursor.getInt(1) == 0) {
                ContentValues valores = new ContentValues();
                valores.put("subido", 1);
                valores.put("localidad", cursor.getString(2));
                valores.put("calle", cursor.getString(3));
                valores.put("numero", cursor.getString(4));
                valores.put("tipo", cursor.getString(5));
                valores.put("precio", cursor.getString(6));
                valores.put("imagenes", cursor.getString(7));

                //update(Uri uri, ContentValues valores, String cond, String[] param)
                getActivity().getContentResolver().update(TablaInmueble.CONTENT_URI,
                        valores,
                        TablaInmueble._ID + " = ?",
                        new String[]{String.valueOf(cursor.getInt(0))});

            }
        }
    }

    public void act_editar(int id){
        Intent i = new Intent(getActivity(),Editar.class);
        Bundle b=new Bundle();
        b.putInt("index", id);
        i.putExtras(b);
        startActivityForResult(i, 1);
    }


    public void act_borrar(final int id){
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle("¿Estas seguro?");
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View vista = inflater.inflate(R.layout.dialogo_borrar, null);
        alert.setView(vista);
        alert.setPositiveButton("borrar",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        borrar(id);
                        ad.notifyDataSetChanged();
                    }
                });
        alert.setNegativeButton("cancelar", null);
        alert.show();
    }

    public void borrar(int id){
        //delete(Uri uri, String cond, String[] param)
        int delete = getActivity().getContentResolver().delete(TablaInmueble.CONTENT_URI,
                TablaInmueble._ID + " = ?",
                new String[] { String.valueOf(id)});
        tostada("Elemento eliminado");
    }

    public void tostada(String s){
        Toast.makeText(getActivity(), s, Toast.LENGTH_SHORT).show();
    }

    public String toString(Cursor c){
        // Inmueble nº __ situado en Las Gabias, c/Flor nº 18
        return "Inmueble nº "+ c.getInt(0)+" situado en " + c.getString(2) + ", c/" + c.getString(3) + " nº " + c.getString(4) + " }";
    }


    /**************** CALLBACK ****************/

    private Callbacks mCallbacks = CallbacksVacios;

    public interface Callbacks {
        public void onItemSelected(int id);
    }

    private static Callbacks CallbacksVacios = new Callbacks() {
        @Override
        public void onItemSelected(int id) {
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Error: La actividad debe implementar el callback del fragmento");
        }
        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = CallbacksVacios;
    }

    private String getUsuarioSharedPreferences() {
        SharedPreferences sp = getActivity().getSharedPreferences("usuario", Context.MODE_PRIVATE);
        return sp.getString("usuario", "");
    }
}
