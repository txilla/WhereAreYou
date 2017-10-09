package com.example.txilla.way;

import android.content.Intent;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

/*
* SettingsActivity se encarga de cargar las preferencias definidas en R.xml.settings. Crea los objetos necesarios para guardar y leer
* los datos de la memoria.
*
* */

public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceClickListener {
    /*Podria ser un fragment i heretar de PreferenceFragment*/
    TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Se cargan los datos del xml para que se creen las preferencias
        //
        addPreferencesFromResource(R.xml.settings);

        Preference wifiPreference = findPreference("Wifi");

        // La wifi preference debe abrir el menu principal al recibir un click.
        //
        wifiPreference.setOnPreferenceClickListener(this);
    }


    @Override
    public boolean onPreferenceClick(Preference preference) {
        final String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;

        // En caso de hacer click sobre wifi, se abre el menu de configuracion del wifi
        //
        if (preference.getKey().equals("Wifi"))
        {
            this.startActivity(new Intent(action));
        }

        return false;
    }
}
