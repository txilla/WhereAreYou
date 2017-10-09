package com.example.txilla.way;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * Created by tXillA on 16/12/2016.
 *
 * Descripcion: Se conecta al servidor y le manda los datos para insertarlos en la base de datos
 */

public class ConnexioHTTP extends AsyncTask<Void, Void, JSONObject> {

    // Atributos de la clase
    //

    // Variables internas de la clase
    private String unaUrl = "";

    //Parametros que se reciben por el constructor
    private String nomServidor = "";
    private String user = "";
    private Context mContext;

    /* Constructor */
    public ConnexioHTTP (String nServidor,String usuario, Context aContext ) {
        nomServidor = nServidor;
        user = usuario;
        mContext = aContext;

        Log.d("Connexio", "Datos recibidos: " + nomServidor + "" + user );
    }

    /*
    * AsyncTask methods
    * */
    @Override
    protected JSONObject doInBackground(Void... params) {

        //Toast.makeText(getBaseContext(),"Please wait, connecting to server.",Toast.LENGTH_LONG).show();
        JSONObject result = null;

        try {

            // URLEncode user defined data (Si obtenim d'un formulari)
            //String user    = URLEncoder.encode(login.getText().toString(), "UTF-8");
            //String user = "Eva";

            // Se decide la URL a la que se va a acceder, para mandar los datos
            if (nomServidor.equals("Arantxa")) {

                unaUrl = "http://cucaracha.esy.es/gps.php?latitud=" + MapsActivity.latitud + "&longitud=" + MapsActivity.longitud + "&altitud=" + MapsActivity.altitud + "&nombre=" + user;
                Log.d("Connexio", "entra por todos: " + user + nomServidor);
            }
            else if (nomServidor.equals("Ruben")) {
                unaUrl = "http://cacatua.esy.es/Practica3/guardarCordenadas.php?usuario="+ user +"&lat="+ MapsActivity.latitud +"&long=" +MapsActivity.longitud;
                Log.d("Connexio", "entra por todos: " + user + nomServidor);
            }
            else if (nomServidor.equals("Xavi")) {
                unaUrl = "http://xavicrm.esy.es/xavi/insert.php?nom="+ user +"&latitud="+ MapsActivity.latitud +"&longitud="+MapsActivity.longitud;
                Log.d("Connexio", "entra por todos: " + user + nomServidor);
            }

            // Se crea la url
            URL url = new URL(unaUrl);
            Log.d("Connexio", "Inici openConnection a la URL: " + url);

            // Se crea una conexion a la url
            HttpURLConnection urlConnection =  (HttpURLConnection)url.openConnection();

            // Estado de la conexion
            int status = urlConnection.getResponseCode();
            Log.d("Connexio", "Estat que retorna la connexió: " + status );

            // Check the connection status
            if (status == 200) // if response code = 200 ok
            {
                // Read the BufferedInputStream
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                Log.d("Connexio", "Recupero InputStream ");

                // Se leen los datos
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();
                Log.d("Connexio", sb.toString());

                // Se desconecta del servidor
                urlConnection.disconnect();
                Log.d("Connexio", "Connexió tancada");

                // Se construye el json con el string que se ha recuperado del servidor
                JSONObject json = new JSONObject(sb.toString());
                result = json;
                Log.d("Connexio", "Json estado: " + json.get("estado"));
            }
            else
            {
                Log.d("Connexio","Estado mal.");
            }

        } catch (UnknownHostException ex)
        {
            Log.d("Connexio", "Entro en excepcion");

        } catch (Exception ex) {
            Log.d("Connexio", "Excepció ");
            ex.printStackTrace();
        }

        // result null significa que ha habido algun error de conexion con el servidor
        return result;
    }

    @Override
    protected void onPostExecute(JSONObject jObject) {

        // Se tratan los errores si los ha habido. Sino no se hace nada porque los datos ya estan guardados en la base de datos
        //
        if (jObject == null)
        {
            Toast.makeText(mContext,
                    "Servidor de dades no disponible.",
                    Toast.LENGTH_LONG).show();
        } else {
            try {
                if (jObject.getInt("estado") != 1) {
                    Toast.makeText(mContext,
                            "No es posible guardar dades al servidor.",
                            Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                Toast.makeText(mContext,
                        "Internal error.",
                        Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }

    }
}








/*

public class ConnexioHTTP extends Thread {


    String nomServidor = "";
    String unaUrl = "";
    String user = "";

    Context mContext;


    public ConnexioHTTP (String nServidor,String usuario, Context aContext ) {
        nomServidor = nServidor;
        user = usuario;
        mContext = aContext;

        Log.d("Connexio", "Datos recibidos: " + nomServidor + "" + user );
    }
    @Override
    public void run() {
        super.run();

        //Toast.makeText(getBaseContext(),"Please wait, connecting to server.",Toast.LENGTH_LONG).show();
        Boolean connexioIncorrecte = false;

        try {

            // URLEncode user defined data (Si obtenim d'un formulari)
            //String user    = URLEncoder.encode(login.getText().toString(), "UTF-8");
            //String user = "Eva";

            if (nomServidor.equals("Arantxa")) {

                unaUrl = "http://cucaracha.esy.es/gps.php?latitud=" + MapsActivity.latitud + "&longitud=" + MapsActivity.longitud + "&altitud=" + MapsActivity.altitud + "&nombre=" + user;
                Log.d("Connexio", "entra por todos: " + user + nomServidor);
            }
            else if (nomServidor.equals("Ruben")) {
                unaUrl = "http://cacatua.esy.es/Practica3/guardarCordenadas.php?usuario="+ user +"&lat="+ MapsActivity.latitud +"&long=" +MapsActivity.longitud;
                Log.d("Connexio", "entra por todos: " + user + nomServidor);
            }
            else if (nomServidor.equals("Xavi")) {
                unaUrl = "http://xavicrm.esy.es/xavi/insert.php?nom="+ user +"&latitud="+ MapsActivity.latitud +"&longitud="+MapsActivity.longitud;
                Log.d("Connexio", "entra por todos: " + user + nomServidor);
            }
                URL url = new URL(unaUrl);
            Log.d("Connexio", "Inici openConnection a la URL: " + url);

            HttpURLConnection urlConnection =  (HttpURLConnection)url.openConnection();
            int status = urlConnection.getResponseCode();

            Log.d("Connexio", "Estat que retorna la connexió: " + status );

            // Check the connection status
            if (status == 200) // if response code = 200 ok
            {

                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                // Read the BufferedInputStream
                BufferedReader br = new BufferedReader(new InputStreamReader(in));

                Log.d("Connexio", "Recupero InputStream ");

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();
                Log.d("Connexio", sb.toString());


                urlConnection.disconnect();
                Log.d("Connexio", "Connexió tancada");

                /@ Ara llegim el JSON @/

                JSONObject json = new JSONObject(sb.toString());
                Log.d("Connexio", "Json estado: " + json.get("estado"));

                //Mostrar toast


                //json = sb.toString();


            }
            else
            {
                Log.d("Connexio","Estado mal.");

            }

        } catch (UnknownHostException ex)
        {
            Log.d("Connexio", "Entro en excepcion");
            connexioIncorrecte = true;

        } catch (Exception ex) {
            Log.d("Connexio", "Excepció ");
            ex.printStackTrace();
            connexioIncorrecte = true;
        }

/@
        if (connexioIncorrecte)
        {

            Toast.makeText(mContext,
                    "GPS desactivat per l'usuari",
                    Toast.LENGTH_LONG).show();

        }
        @/
    }

}
*/