package com.example.txilla.way;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by tXillA on 20/12/2016.
 *
 * Descripcion: Recupera datos del servidor segun las preferencias
 *
 */

public class leerJSON extends AsyncTask<GoogleMap, Void, JSONObject> {

    // Atributos de la clase
    //

    // Atributos del constructor
    private String hacer = "";
    private Context mContext;
    private String user = "";

    // Parametros del execute
    private GoogleMap mMap;

    // Variables internas de la clase
    private String unaUrl="";
    private LatLng anterior;
    private boolean hayMarcador=false;

    private final float colores[] = {BitmapDescriptorFactory.HUE_GREEN, BitmapDescriptorFactory.HUE_BLUE, BitmapDescriptorFactory.HUE_ORANGE,
            BitmapDescriptorFactory.HUE_RED, BitmapDescriptorFactory.HUE_ROSE, BitmapDescriptorFactory.HUE_VIOLET,
            BitmapDescriptorFactory.HUE_YELLOW, BitmapDescriptorFactory.HUE_CYAN, BitmapDescriptorFactory.HUE_MAGENTA,
            BitmapDescriptorFactory.HUE_AZURE};
    private int contadorColores=0;

    /* Constructor */
    public leerJSON (String opcion, String usuario,  Context aContext ) {
        user = usuario;
        hacer = opcion;
        mContext = aContext;

        Log.d("Devolver", "Datos recibidos: " + user + " " + hacer);
    }


    /*
    * AsyncTask methods
    * */
    @Override
    protected JSONObject doInBackground(GoogleMap... params) {
        // Se recibe el mapa que se ha mandado desde el "execute"
        mMap = params[0];

        JSONObject result = null;

        try {
            // Se decide el servidor al que se va a acceder
            if (hacer.equals("Todos")) {
                unaUrl = "http://cucaracha.esy.es/devolver.php?todo=todos";
                Log.d("Devolver", "entra por todos: " + user + " " + hacer);
            }
            else if (hacer.equals("Usuario")) {
                unaUrl = "http://cucaracha.esy.es/devolver.php?todo=user&user=" + user;
                Log.d("Devolver", "entra por usuario " + user + " " + hacer);
            }
            // URLEncode user defined data (Si obtenim d'un formulari)
            //String user    = URLEncoder.encode(login.getText().toString(), "UTF-8");
            //String user = "arantxa";
            Log.d("Devolver", "Datos recibidos caca: " + user + " " + hacer);

            // Se crea la url
            //URL url = new URL("http://cucaracha.esy.es/devolver.php?todo=user&user="+user);
            URL url = new URL(unaUrl);
            Log.d("Devolver", "Inici openConnection a la URL: " + url);

            // Se crea una conexion a la url
            HttpURLConnection urlConnection =  (HttpURLConnection)url.openConnection();

            // Se recupera el estado de la conexion
            int status = urlConnection.getResponseCode();
            Log.d("Devolver", "Estat que retorna la connexió: " + status );

            // Check the connection status
            if (status == 200) // if response code = 200 ok
            {
                // Read the BufferedInputStream
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                Log.d("Devolver", "Recupero InputStream ");

                // Se leen los datos
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();
                Log.d("Devolver", sb.toString());

                // Se desconecta del servidor
                urlConnection.disconnect();
                Log.d("Devolver", "Connexió tancada");

                /* Ara llegim el JSON */

                //JSONObject json = new JSONObject(sb.toString());
                //Log.d("Connexio", "Json estado: " + json.get("estado"));

                //Mostrar toast


                //json = sb.toString();

                // Se procesan los datos que deberian estar en formato JSON
                result = new JSONObject(sb.toString());
                //result = jObject;
            }
            else
            {
                Log.d("Devolver","Estado mal");
            }

        } catch (Exception ex) {
            Log.d("Devolver", "Excepció ");
            ex.printStackTrace();
        }

        /* result null significa que ha habido algun error*/
        return result;
    }

    @Override
    protected void onPostExecute(JSONObject jObject) {
        //Carniceria  set num pollos =+1
        //image.setImageBitmap(result);
        //Log.d("Connexio", "Imatge al lloc " + image.getId());
        //image.setVisibility(View.VISIBLE);

        boolean ok = true;

        // Se tratan los errores
        //
        if (jObject == null) {
            Toast.makeText(mContext,
                    "Servidor de dades no disponible.",
                    Toast.LENGTH_LONG).show();
            ok = false;

        }else {
            try {
                if (jObject.getInt("estado") != 1) {
                    Toast.makeText(mContext,
                            "No es posible llegir dades del servidor.",
                            Toast.LENGTH_LONG).show();
                    ok = false;
                }
            } catch (JSONException e) {
                Toast.makeText(mContext,
                        "Internal error.",
                        Toast.LENGTH_LONG).show();
                e.printStackTrace();
                ok = false;
            }

        }

        // Si no hay errores, se leen los datos del JSON
        if (ok) {
            try {

                // builder va a contener los puntos para poder hacer zoom en el mapa donde se vean todos los puntos
                LatLngBounds.Builder builder = new LatLngBounds.Builder();

                for (int i = 0; i < jObject.length() - 1; i++) {

                    // Se lee el JSON
                    double latitud = Double.parseDouble(jObject.getJSONObject("" + i).getString("latitud"));
                    double longitud = Double.parseDouble(jObject.getJSONObject("" + i).getString("longitud"));
                    String titol = jObject.getJSONObject("" + i).getString("nom");

                    // Se crea un punto y se anado al builder
                    LatLng punt = new LatLng(latitud, longitud);
                    builder.include (punt);
                    Log.d("ConnexioD", "Nou marker a: " + latitud + titol);

                    // Se muestran en el mapa los puntos segun las preferencias
                    if (hacer.equals("Todos")) {
                        mMap.addMarker(new MarkerOptions().position(punt).title(titol).icon(BitmapDescriptorFactory.defaultMarker(colores[contadorColores])).snippet("Latitud: " + latitud + " Longitud: " + longitud));

                    } else if (hacer.equals("Usuario")) {
                        mMap.addMarker(new MarkerOptions().position(punt).title(titol).icon(BitmapDescriptorFactory.defaultMarker(colores[0])).snippet("Latitud: " + latitud + " Longitud: " + longitud));
                        if (hayMarcador) {
                            mMap.addPolyline(new PolylineOptions().geodesic(true)
                                    .add(anterior)
                                    .add(punt)
                                    .color(Color.RED));
                        }
                        anterior = punt;
                        hayMarcador = true;
                    }
                    contadorColores++;
                    if (contadorColores >= 10) {
                        contadorColores = 0;
                    }
                }

                // Se hace zoom en el mapa para que se vean todos los puntos
                //
                LatLngBounds bounds = builder.build();
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 100);

                mMap.moveCamera(cu);
                mMap.animateCamera(cu);

            } catch (Exception ex) {
                Log.d("Devolver", "Excepció ");
                ex.printStackTrace();
            }
        }
    }
}
