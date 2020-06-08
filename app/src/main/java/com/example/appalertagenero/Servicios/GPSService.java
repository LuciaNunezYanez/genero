package com.example.appalertagenero.Servicios;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.appalertagenero.R;
import com.example.appalertagenero.Utilidades.Utilidades;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static com.example.appalertagenero.Constantes.CHANNEL_ID;
import static com.example.appalertagenero.Constantes.ID_SERVICIO_AUDIO;

public class GPSService extends Service {

    private LocationManager mlocManager;
    Intent intentG;
    String TAG = "GPSService";
    Localizacion Local;

    // VARIABLES
    int reporteGenerado;
    String nombrePadre;
    String fechaActual;

    public GPSService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("onBind no implementado");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent!=null){
            intentG = intent;
            reporteGenerado = intent.getIntExtra("reporteGenerado",0);
            nombrePadre = intent.getStringExtra("padre");
            if(reporteGenerado!=0){
                locationStart();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void locationStart() {
        mlocManager = (LocationManager) getSystemService(getApplicationContext().LOCATION_SERVICE);
        Local = new Localizacion();
        final boolean gpsEnabled = mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        // PRUEBA
        Intent intent = new Intent("GPSService");
        intent.putExtra("fecha", fechaActual);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

        if (!gpsEnabled) {
            // GPS Desactivado
            darResultados(getApplicationContext(),0.0, 0.0, "GPS Deshabilitado");
        } else {
            try{
                mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, Local, Looper.myLooper());
            }catch (java.lang.SecurityException ex) {
                Log.i(TAG, "Network "+ "no solicitar la actualización de ubicación: " + ex.getMessage());
            } catch (IllegalArgumentException ex) {
                Log.d(TAG, "Network "+ "proveedor de red no existe, " + ex.getMessage());
            }
            try{
                mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, Local, Looper.myLooper());
            }catch (java.lang.SecurityException ex) {
                Log.i(TAG, "GPS "+ "no solicitar la actualización de ubicación: " + ex.getMessage());
                darResultados(getApplicationContext(),0.0, 0.0, "No se pudo obtener la ubicación actual");
            } catch (IllegalArgumentException ex) {
                Log.d(TAG, "GPS "+ "proveedor de red no existe, " + ex.getMessage());
                darResultados(getApplicationContext(),0.0, 0.0, "El proveedor GPS no existe");
            }
        }
    }


    public class Localizacion implements LocationListener {
        @Override
        public void onLocationChanged(Location loc) {
            Log.d(TAG, "GPS "+ loc.getLatitude());
            Log.d(TAG, "GPS "+ loc.getLongitude());
            fechaActual = Utilidades.obtenerFecha();
            mlocManager.removeUpdates(Local);
            try {
                Local.finalize();
            } catch (Throwable throwable) {
                Log.d(TAG, "GPS "+ "Error al finalizar");
                throwable.printStackTrace();
            }

            String direccionCompleta = "";
            try {
                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                List<Address> list = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);

                /*if (!list.isEmpty()) {
                    Address direccion = list.get(0);
                    Log.d(TAG, direccion.toString());
                    list.get(0).get
                    direccionCompleta = direccion.getAddressLine(0);
                    Log.d(TAG, "La dirección completa es: " + direccionCompleta);
                }*/
            } catch (IOException e) {
                Log.e(TAG, "Error #8: " + e.getMessage());
                e.printStackTrace();
            }

            darResultados(getApplicationContext(), loc.getLatitude(), loc.getLongitude(), "Se localizó ubicación");
            stopSelf();
        }


        @Override
        public void onProviderDisabled(String provider) {
            // Este metodo se ejecuta cuando el GPS es desactivado
            Toast.makeText(getApplicationContext(), "Error #8: GPS Desactivado", Toast.LENGTH_LONG).show();
            Log.d(TAG, "GPS Desactivado");
        }
        @Override
        public void onProviderEnabled(String provider) {
            // Este metodo se ejecuta cuando el GPS es activado
            Toast.makeText(getApplicationContext(), "Error #8: GPS Activado", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "GPS Activado");
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d(TAG, "GPS "+ "El estatus cambió");
        }
    }


    private void darResultados(Context context, Double latitud, Double longitud, String mensaje){
        Intent intent = new Intent("GPSService");
        intent.putExtra("fecha", fechaActual);
        intent.putExtra("latitud", latitud);
        intent.putExtra("longitud",  longitud);
        intent.putExtra("padre", nombrePadre);
        intent.putExtra("reporteCreado", reporteGenerado);
        intent.putExtra("mensaje", mensaje);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        stopSelf();
    }
}
