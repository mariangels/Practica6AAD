package com.example.mariangeles.practica6aad;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import android.support.v4.app.FragmentActivity;

import com.example.mariangeles.practica6aad.Datos.TablaInmueble;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ActividadLista extends FragmentActivity implements FragmentoLista.Callbacks{

    private boolean dosFragmentos;
    private Cursor c;
    public static String BASE="http://192.168.1.102:52023/Practica6AAD2/ControlAndroid";

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.actividad_lista);
        setUsuarioSharedPreferences("mariam");
        if (findViewById(R.id.fragmentoDetalle) != null) {
            dosFragmentos = true;
        }else{
            dosFragmentos = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result = false;
        c = getContentResolver().query(TablaInmueble.CONTENT_URI,
                TablaInmueble.COLUMNAS,
                null, null, null);
        switch(item.getItemId()) {
            case R.id.nuevo:
                result=true;
                Intent intent = new Intent(this, Editar.class);
                Bundle bundle=new Bundle();
                bundle.putInt("index",-1);
                intent.putExtras(bundle);
                startActivity(intent);
                break;

            case R.id.actualizar:
                //actualizar
                //...
                new Actualizar().execute();
                break;
            case R.id.subir:
                //subir muchos
                //...
                new Subir().execute();
                break;
        }
        return result;
    }

    class Subir extends AsyncTask<String,Integer,String> {

        @Override
        protected String doInBackground(String... strings) {
            c.moveToFirst();
            String r="";
            for(int i=0; i<c.getCount(); i++) {
                c.moveToPosition(i);
                if (c.getInt(1) == 0) {
                    //no esta subido
                    String url = "?target=prueba" + "&op=insert" + "&action=op";

                    //se sube
                    r =post(c,BASE + url);

                    //si tiene imagen

                    if(!c.getString(7).equalsIgnoreCase("sin imagen")) {
                        File imgFile = new File(getExternalFilesDir(null), c.getString(7));
                        String a = "http://192.168.1.102:52023/Practica6AAD2/subirimagen";
                        postFile(a, "archivo", imgFile);
                    }
                }
            }
            return r;
        }

        @Override
        protected void onPostExecute(String r) {
            super.onPostExecute(r);
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
            cursor.moveToFirst();
            for(int i=0; i<cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                if (cursor.getInt(1) == 0) {
                    ContentValues valores= new ContentValues();
                    valores.put("subido", 1);
                    valores.put("localidad", cursor.getString(2));
                    valores.put("calle", cursor.getString(3));
                    valores.put("numero", cursor.getString(4));
                    valores.put("tipo", cursor.getString(5));
                    valores.put("precio", cursor.getString(6));
                    valores.put("imagenes",cursor.getString(7));

                    //update(Uri uri, ContentValues valores, String cond, String[] param)
                    getContentResolver().update(TablaInmueble.CONTENT_URI,
                            valores,
                            TablaInmueble._ID + " = ?",
                            new String[]{String.valueOf(cursor.getInt(0))});
                }
            }
        }
    }

    class Actualizar extends AsyncTask<String,Integer,String> {

        @Override
        protected String doInBackground(String... params) {
            String url="?target=prueba&"+"op=select&"+"action=view";
            String r=leerpagina(BASE+url);
            return r;
        }

        @Override
        protected void onPostExecute(String strings) {
            super.onPostExecute(strings);
            leerJSON(strings);
            tostada(strings);
        }

        public void leerJSON(String s){
            JSONTokener token = new JSONTokener(s);
            try {
                JSONArray array= new JSONArray(token);
                for(int i=0;i<array.length(); i++){
                    JSONObject fila = array.getJSONObject(i);

                    boolean distinto=true;
                    c.moveToFirst();
                    for (int j = 0; j < c.getCount(); j++) {
                        c.moveToPosition(j);
                        if (c.getString(2).equals(fila.getString("localidad")) &&
                            c.getString(3).equals(fila.getString("calle")) &&
                            c.getString(4).equals(fila.getString("numero")) &&
                            c.getString(5).equals(fila.getString("tipo")) &&
                            c.getString(6).equals(fila.getString("precio"))) {

                            distinto=false;
                        }
                    }
                    if(distinto) {
                        ContentValues valores = new ContentValues();
                        valores.put("subido", 1);
                        valores.put("localidad", fila.getString("localidad"));
                        valores.put("calle", fila.getString("calle"));
                        valores.put("numero", fila.getString("numero"));
                        valores.put("tipo", fila.getString("tipo"));
                        valores.put("precio", fila.getString("precio"));
                        valores.put("imagenes", "sin imagen");
                        //guardamos sin imagen
                        getContentResolver().insert(TablaInmueble.CONTENT_URI, valores);
                    }
                }
            } catch (JSONException e) {
                Log.v("ser","noseer");
            }
        }

        public String leerpagina(String data){
            URL url;
            InputStream is = null;
            BufferedReader br;
            String line,out="";
            try{
                url = new URL(data);
                is = url.openStream();  // throws an IOException
                br = new BufferedReader(new InputStreamReader(is));
                while ((line = br.readLine()) != null) {
                    out+=line+"\n";
                }
                br.close();
                is.close();
                return out;
            }catch(IOException e){
                System.out.println(e);
            }
            return "no se ha podido leer";
        }
    }

    @Override
    public void onItemSelected(int id) {
        if(dosFragmentos){
            FragmentoDetalle fragmento= (FragmentoDetalle) getSupportFragmentManager().findFragmentById(R.id.fragmentoDetalle);
            if (fragmento != null && fragmento.isInLayout()) {
                fragmento.mostrar(id);
            }

        }else{
            Intent i = new Intent(this, ActividadDetalle.class);
            i.putExtra("index", id);
            startActivity(i);
        }
    }

    private String getUsuarioSharedPreferences() {
        SharedPreferences sp = getSharedPreferences("usuario", Context.MODE_PRIVATE);
        return sp.getString("usuario", "");
    }

    private void setUsuarioSharedPreferences(String usuario) {
        SharedPreferences sp = getSharedPreferences("usuario", Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putString("usuario", usuario);
        ed.apply();
    }

    public  void tostada(String s){
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
}


