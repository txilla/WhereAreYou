package com.example.txilla.way;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/*
 Esta es la clase principal. Implementa las interfaces:
 a) OnMapReadyCallback
    -> Recibe notificaciones cada vez que el mapa esta disponible. Se usa para cargar los datos por primera vez cuando la app arranca de zero.

 b) LocationListener
    -> Recibe notificaciones al habilitar/deshabilitar el gps y tambien cuando la posicion del gps cambia

 c) View.OnClickListener
    -> Permite recibir los eventos de "click". Se ysa para el boton de las preferencias.


 d) OnSharedPreferenceChangeListener
    -> proporciona el metodo¨onSharedPreferenceChanged que permite recibir un evento cuando alguna de las preferencias ha cargado.
     Si esto pasa, se guardan en los atributos de la clase mediante el metodo readListUsuarios.

 */


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, View.OnClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

    // Atributos de la clase
    //

    // Atributos publicos accedidos directamente desde ConnexioHTTP
    public static double latitud;
    public static double longitud;
    public static double altitud;

    // mapa de google maps
    private GoogleMap mMap;

    // boton de settings
    private FloatingActionButton settings;

    // una copia local de las preferencias, para no tener que leerlas cada vez que se usan
    private String username;
    private String listUsuarios;
    private String listServidor;
    private boolean checkBox;

    /*
    * FragmentActivity methods. Se llama cuando se crea la actividad, despues del constructor
    * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // onCreate del padre
        super.onCreate(savedInstanceState);

        // objetos que se muestran al usuario
        setContentView(R.layout.activity_maps);

        // Se entera del evento "on click" del boton
        settings = (FloatingActionButton) findViewById(R.id.buttonSettings);
        settings.setOnClickListener(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        // Se llama al "onMapReady" cuando el mapa este disponible
        mapFragment.getMapAsync(this);

        // Se recupera el gestor de locations
        LocationManager gestorLoc = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Se pide permisos de android si se puede se llama a "requestLocationUpdates" para recibir
        // notificaciones cuando la posicion cambie
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED)
            gestorLoc.requestLocationUpdates(LocationManager.GPS_PROVIDER, 300000, 1, this);
        else
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION}, 4);


        // Para recibir notificaciones cuando cambien las preferencias
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);

        // Se cargan los atributos de preferencias por primera vez
        readListUsuarios();
    }


    /*
    * OnMapReadyCallback methods. Se llama cuando los mapas estan disponibles
    * */

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //Location location= new Location();

        //latitud = location.getLatitude();
        //longitud =location.getLongitude();

        // Add a marker in Sydney and move the camera
        /*LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        mMap.getUiSettings().setZoomControlsEnabled(true);*/

        mMap.clear();

        forzarLocalizacion();
    }

   /*
    * LocationListener methods.
    * */
    @Override
    public void onLocationChanged(Location location) {
        // Esta funcion se llama cada vez que la posicion cambia
        
        //readListUsuarios ();
        latitud = location.getLatitude();
        longitud =location.getLongitude();
        altitud = location.getAltitude();

        // Se resetea el mapa
        mMap.clear();

        // Si posicion actual esta marcada, se muestra en el mapa
        if ( listUsuarios.equals("Posicion Actual")) {
            
            // Se coloca el marker en la posicion actual y se enfoca la camara
            //
            LatLng myLocation = new LatLng(latitud, longitud);
            mMap.addMarker(new MarkerOptions().position(myLocation).title("Marker in my location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(myLocation, 15);
            mMap.animateCamera(cameraUpdate);

            // Se muestra la posicion en un toast
            //
            String text = "Posició actual:\n" +
                    "Latitud = " + latitud + "\n" +
                    "Longitud = " + longitud+ "\n" +
                    "Altitud = " + altitud;

            Toast.makeText(getApplicationContext(), text,
                    Toast.LENGTH_LONG).show();

        }

        // Se inserta la posicion actual en la base de datos
        insertarDatos ();

        // Se leen los datos del usuario actual y/o otros usuarios de la base de datos y se muestran en el mapa
        mostrarUsuarios();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Esta funcion se llama cuando el estado del gps cambia. Se muestra una toast al usuario notificandole estos cambios.

        String missatge = "";
        switch (status) {
            case LocationProvider.OUT_OF_SERVICE:
                missatge = "GPS status: Out of service";
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                missatge = "GPS status: Temporarily unavailable";
                break;
            case LocationProvider.AVAILABLE:
                missatge = "GPS status: Available";
                break;
        }

        Toast.makeText(getApplicationContext(),
                missatge,
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onProviderEnabled(String provider) {
        // Esta funcion se llama cuando el usuario habilita el GPS.

        Toast.makeText(getApplicationContext(),
                "GPS habilitat per l'usuari",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        // Esta funcion se llama cuando el usuario deshabilita el GPS.

        // meter lo del jordi
        Toast.makeText(getApplicationContext(),
                "GPS desactivat per l'usuari",
                Toast.LENGTH_LONG).show();
    }


    /*
    * View.OnClickListener methods
    * */
    @Override
    public void onClick(View v) {
        // Esta funcion se llama cuando se hace click sobre los objetos del mapa. 
        
        // Si se hace click sobre settings, se crea una actividad para abrir el menu
        if ((FloatingActionButton)v==settings) {
            Intent intent = new Intent (getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
        }
    }
    
    /*
     * SharedPreferences.OnSharedPreferenceChangeListener methiods
     */
    @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Si el usuario ha cambiaodo cualquier preferencia, se llama a esta funcion
        
        // Se actualizan los atributos de la clase con las preferencias
        readListUsuarios ();

        // Se lee una nueva posicion y se trata
        forzarLocalizacion();
    }

    /*
    * Metodos privados de la clase
    * */

    /* Este metodo recupera la ultima posicion valida y la utiliza para mostrar los datos por pantalla */
    private void forzarLocalizacion() {

        String locationProvider = LocationManager.GPS_PROVIDER;
        
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        
        // Si no se tiene permisos para acceder al gps, se piden
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            
            return;
        }

        // Si hay permisos para acceder al gps, se recupera la ultima posicion valida
        Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);

        // Se pasa la localizacion obtenida
        onLocationChanged(lastKnownLocation);
    }

    /* Este metodo recupera el JSON con los datos de la base de datos y los muestra en el mapa */
    private void mostrarUsuarios ()
    {
        //leerJSON devolver=new leerJSON(listUsuarios,username);
        //devolver.start();

        // Solo se recuperan los datos de la base de datos cuando la configuracion es "Todos" o "Usuario"
        if ( listUsuarios.equals("Todos") || listUsuarios.equals("Usuario")) {
            leerJSON devolver = new leerJSON(listUsuarios, username, getApplicationContext());
            devolver.execute(mMap);
        }
    }

    /* Este metodo utiliza la clase ConnexioHTTP par mandar los datos a la base de datos e insertarlos en la table*/
    private void insertarDatos()
    {
        // Se mandan los datos de usuario a la base de datos cuando no esta configurado "de incognito"
        if (checkBox==false) {
            ConnexioHTTP connexio = new ConnexioHTTP(listServidor, username, this);
            connexio.execute();
        }
    }

    /* Se leen las preferencias y se cargan en los atibutos de la clase*/
    private void readListUsuarios () {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Se guardan las preferencias en los atributos de la clase
        username     = prefs.getString("username", "arantxa");
        listUsuarios = prefs.getString("listUsuarios", "Posicion Actual");
        listServidor = prefs.getString("listServidores", "Arantxa");
        checkBox     = prefs.getBoolean("checkBox", false);

        //Toast.makeText(getApplicationContext(), listUsuarios,
        //        Toast.LENGTH_LONG).show();
    }
}
