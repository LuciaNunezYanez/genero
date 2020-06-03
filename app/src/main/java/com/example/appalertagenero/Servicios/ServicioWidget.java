package com.example.appalertagenero.Servicios;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.appalertagenero.R;
import com.example.appalertagenero.Utilidades.EnviarCoordenadas;
import com.example.appalertagenero.Utilidades.EnviarImagenes;
import com.example.appalertagenero.Utilidades.Notificaciones;
import com.example.appalertagenero.Utilidades.PreferencesCiclo;
import com.example.appalertagenero.Utilidades.PreferencesReporte;
import com.example.appalertagenero.Utilidades.Utilidades;

import static com.example.appalertagenero.Constantes.CHANNEL_ID;
import static com.example.appalertagenero.Constantes.ID_SERVICIO_WIDGET_CREAR_REPORTE;
import static com.example.appalertagenero.Constantes.ID_SERVICIO_WIDGET_GENERAR_ALERTA;

public class ServicioWidget extends Service {

    String TAG = "ServicioWidget";

    // VARIABLES PARA USO DE MULTIMEDIA
    ImageTraseraResultReceiver resultTReceiver;
    ImageFrontalResultReceiver resultFReceiver;
    int ciclo = 1;

    public static final int SUCCESS = 1;
    public static final int ERROR = 0;

    Boolean procesoImagenFrontal = false;
    Boolean procesoImageTrasera = false;

    String IMAGEN_FRONTAL = "Ninguna";
    String IMAGEN_TRASERA = "Ninguna";

    String FECHA_FRONTAL = "";
    String FECHA_TRASERA = "";

    int reporteCreado = 0;
    int idComercio = 0;
    int idUsuario = 0;

    Intent intentGPS;
    Notificaciones notificaciones = new Notificaciones();

    public ServicioWidget() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        iniciarProceso();
        crearNotificacionPersistente(getApplicationContext(), ServicioWidget.class, CHANNEL_ID, "Botón de pánico activado", "Botón activado desde el widget. Enviando multimedia..", R.drawable.ic_notification_siren_2, "Descripción", ID_SERVICIO_WIDGET_CREAR_REPORTE);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void iniciarProceso(){
        Boolean puedeEnviar = PreferencesReporte.puedeEnviarReporte(getApplicationContext(), System.currentTimeMillis());
        Log.d("REPORTE", "Puede enviar?? " + puedeEnviar);

        if(puedeEnviar){
            PreferencesReporte.guardarReporteInicializado(getApplicationContext());
            LocalBroadcastManager.getInstance(getApplication()).registerReceiver(broadcastReceiverGenerarAlerta, new IntentFilter("generarAlertaService"));
            // Registrar escuchadores
            registrarReceiverGnerarAlerta();
            registrarEscuchadorGPS();
            // Iniciar el servicio GenerarAlertaService
            iniciarServicioGenerarAlerta();
        } else{
            deboTerminar();
            Log.d(TAG, "NO PUEDE GENERAR REPORTE POR QUE HAY UNO PENDIENTE!!");
        }
    }

    public void crearNotificacionPersistente(Context context, Class clase, String canal, String titulo, String mensaje, int icono, String descripcion, int idServicio){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Crear notificación de servicio activo
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, clase), 0);
            Notification notification =
                    new Notification.Builder(context, canal)
                            .setColor(Color.WHITE)
                            .setContentTitle(titulo)
                            .setContentText(mensaje)
                            .setColor(Color.RED)
                            .setSmallIcon(icono)
                            .setContentIntent(pendingIntent)
                            .build();

            // Crear al canal de notificación, pero solo en API 26+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "Botón de pánico para comercios", NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.setDescription(descripcion);
                NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(notificationChannel);
            }

            startForeground(idServicio, notification);
            Log.d(TAG,"Se inicio servicio para API 26+ - Desde servicio prueba!");

        } else{
            // Mostrar el otro tipo de notificación
        }
    }

    /**********************************************************************************************
     *                                 GENERAR  ALERTA                                             *
     **********************************************************************************************/

    public void registrarReceiverGnerarAlerta(){
        LocalBroadcastManager.getInstance(getApplication()).registerReceiver(broadcastReceiverGenerarAlerta, new IntentFilter("generarAlertaService"));
    }

    private void iniciarServicioGenerarAlerta(){
        SharedPreferences preferences = getSharedPreferences("Login", Context.MODE_PRIVATE);
        if (preferences.contains("comercio") && preferences.contains("usuario")){
            idComercio = preferences.getInt("comercio" ,0);
            idUsuario = preferences.getInt("usuario", 0);
        } else {
            // No se puede agregar la notificación
        }

        Intent intent = new Intent(getApplicationContext(), GenerarAlertaService.class);
        intent.putExtra("comercio", idComercio);
        intent.putExtra("usuario", idUsuario);
        intent.putExtra("sala", "Comercios");
        intent.putExtra("fecha", Utilidades.obtenerFecha());
        intent.setPackage("com.id.socketio");
        getApplicationContext().startService(intent);
    }

    public void detenerServicioAlerta(){
        Intent intent = new Intent(getApplicationContext(), GenerarAlertaService.class);
        getApplicationContext().stopService(intent);
    }


    /**********************************************************************************************
     *                                      FOTOGRAFIAS                                            *
     **********************************************************************************************/

    public void iniciarProcesoFotografias(){
        if( getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT) ){
            // INICIAR PROCESO CAMARA FRONTAL
            resultFReceiver = new ImageFrontalResultReceiver(new Handler());
            Intent intentFrontal = new Intent(getApplicationContext(), FotografiaService.class);
            intentFrontal.putExtra("reporteCreado", reporteCreado);
            intentFrontal.putExtra("tipoCamara", "frontal");
            intentFrontal.putExtra("receiver", resultFReceiver);
            getApplicationContext().startService(intentFrontal);

        } else if ( getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA) ){
            // INICIAR PROCESO CAMARA TRAERA
            resultTReceiver = new ImageTraseraResultReceiver(new Handler());
            Intent intentTrasera = new Intent(getApplicationContext(), FotografiaService.class);
            intentTrasera.putExtra("reporteCreado", reporteCreado);
            intentTrasera.putExtra("tipoCamara", "trasera");
            intentTrasera.putExtra("receiver", resultTReceiver);
            getApplicationContext().startService(intentTrasera);

        } else {
            if( procesoImagenFrontal ){
                // ENVIA LA IMAGEN FRONTAL (2)
                terminarServicioFotografias();
                Boolean resultadoenvio = EnviarImagenes.enviarImagenFrontal(getApplicationContext(), IMAGEN_FRONTAL, FECHA_FRONTAL, reporteCreado);
                //calcularNuevoEnvioFotografias();
            } else{
                // TERMINA PROCESO Y NO ENVIA NADA (3)
                finProcesoFotografias();
            }
        }
    }

    public void terminarServicioFotografias(){
        Intent intent = new Intent(getApplicationContext(), FotografiaService.class);
        getApplicationContext().stopService(intent);
    }

    public void finProcesoFotografias(){
        Log.d(TAG, "Termina proceso vacio de fotografias");
        // Al menos guardar las fotografía en el dispositivo
    }

    /*********************************************************************************************
     *                                        GPS                                                 *
     *********************************************************************************************/

    public void comenzarGPS(){
        intentGPS = new Intent(getApplicationContext(), GPSService.class);
        intentGPS.putExtra("reporteGenerado", reporteCreado);
        intentGPS.putExtra("padre", "ServicioWidget");
        startService(intentGPS);
    }

    public void terminarServicioGPS(){
        Intent intent = new Intent(getApplicationContext(), GPSService.class);
        getApplicationContext().stopService(intent);
    }

    /*********************************************************************************************
     *                                         AUDIO                                              *
     *********************************************************************************************/

    public void comenzarGrabacionAudio(){
        registrarEscuchadorAudio();
        Intent intent = new Intent(getApplicationContext(), AudioService.class);
        intent.putExtra("nombreAudio", "GrabacionBotonDePanico");
        intent.putExtra("reporteCreado", reporteCreado);
        getApplicationContext().startService(intent);
    }

    public void terminarGrabacionAudio(){
        eliminarEscuchadorAudio();
        Intent intent = new Intent(getApplicationContext(), AudioService.class);
        getApplicationContext().stopService(intent);
    }

    /*********************************************************************************************
     *                     INICIAN RESULT RECEIVER DE FOTOGRAFIAS                                 *
     *********************************************************************************************/

    private class ImageTraseraResultReceiver extends ResultReceiver {
        public ImageTraseraResultReceiver(Handler handler) {
            super(handler);
        }
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            procesoImageTrasera = true;
            FECHA_TRASERA = resultData.getString("fecha");
            Log.d(TAG, "La fecha de trasera es: " + FECHA_TRASERA);

            // GUARDAR LA IMAGEN DE RETORNO
            String imagenTrasera = resultData.getString("imagen");
            if(!imagenTrasera.equals("Ninguna")){
                IMAGEN_TRASERA = imagenTrasera;
            }

            switch (resultCode) {
                case ERROR:
                    Log.d(TAG, "Ocurrió un error al devolver camara trasera");
                    break;

                case SUCCESS:
                    // ENVIAR MULTIMEDIA (1)
                    terminarServicioFotografias();
                    if(procesoImagenFrontal){
                        Boolean res = EnviarImagenes.enviarImagenFrontal(getApplicationContext(), IMAGEN_FRONTAL, FECHA_FRONTAL, reporteCreado);
                        Log.d(TAG, "El resultado del envio de frontal es: "+ res);
                    }
                    if(procesoImageTrasera){
                        Boolean res = EnviarImagenes.enviarImagenTrasera(getApplicationContext(), IMAGEN_TRASERA, FECHA_TRASERA, reporteCreado);
                        Log.d(TAG, "El resultado del envio de trasera es: "+ res);
                    }

                    //calcularNuevoEnvioFotografias();
                    break;
            }
            super.onReceiveResult(resultCode, resultData);
        }
    }

    private class ImageFrontalResultReceiver extends ResultReceiver {
        public ImageFrontalResultReceiver(Handler handler) {
            super(handler);
        }
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            procesoImagenFrontal = true;
            FECHA_FRONTAL = resultData.getString("fecha");
            Log.d(TAG, "La fecha de frontal es: " + FECHA_FRONTAL);

            // GUARDAR LA IMAGEN DE RETORNO
            String imagenFrontal = resultData.getString("imagen");
            if(!imagenFrontal.equals("Ninguna")){
                IMAGEN_FRONTAL = imagenFrontal;
            }

            switch (resultCode) {
                case ERROR:

                    Log.d(TAG, "Ocurrió un error al devolver cámara frontal");
                    // SE SIGUE CON LA TRASERA
                    break;
                case SUCCESS:

                    if(getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
                        // INICIAR PROCESO CAMARA TRAERA
                        resultTReceiver = new ImageTraseraResultReceiver(new Handler());
                        Intent intentTrasera = new Intent(getApplicationContext(), FotografiaService.class);
                        intentTrasera.putExtra("reporteCreado", reporteCreado);
                        intentTrasera.putExtra("tipoCamara", "trasera");
                        intentTrasera.putExtra("receiver", resultTReceiver);
                        startService(intentTrasera);
                    }else {
                        if(procesoImagenFrontal){
                            // ENVIAR LA IMAGEN FRONTAL (5)
                            terminarServicioFotografias();
                            Boolean res = EnviarImagenes.enviarImagenFrontal(getApplicationContext(), IMAGEN_FRONTAL, FECHA_FRONTAL, reporteCreado);

                            //calcularNuevoEnvioFotografias();
                            Log.d(TAG, "El resultado del envio de frontal es: "+ res);
                        } else{
                            // FIN DEL PROCESO (6)
                            finProcesoFotografias();
                        }
                    }
                    break;
            }
            super.onReceiveResult(resultCode, resultData);
        }
    }

    /*public void calcularNuevoEnvioFotografias(){
        ciclo--;
        Log.d(TAG, "CICLO: "+ ciclo);

        if(ciclo != 0){
            // Iniciar nuevamente el proceso de fotografias
            iniciarProcesoFotografias();
        } else if (ciclo == 0){
            terminarServicioFotografias();
            deboTerminar();
        }
    } */

    /*********************************************************************************************
     *                       BROADCAST RECEIVERS - ESCUCHADORES                                   *
     *********************************************************************************************/
    public void registrarEscuchadorGPS(){
        LocalBroadcastManager.getInstance(getApplication()).registerReceiver(broadcastReceiverGPSService, new IntentFilter("GPSService"));
    }

    public void eliminarEscuchadorGPS(){
        LocalBroadcastManager.getInstance(getApplication()).unregisterReceiver(broadcastReceiverGPSService);
    }

    public void registrarEscuchadorAudio(){
        LocalBroadcastManager.getInstance(getApplication()).registerReceiver(broadcastReceiverAudio, new IntentFilter("AudioBroadcast"));
    }

    public void eliminarEscuchadorAudio(){
        LocalBroadcastManager.getInstance(getApplication()).unregisterReceiver(broadcastReceiverAudio);
    }

    private BroadcastReceiver broadcastReceiverGenerarAlerta = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intentGenerarAlerta) {

            try{
                Bundle parametros = intentGenerarAlerta.getExtras();
                reporteCreado = parametros.getInt("reporteCreado", 0);

                detenerServicioAlerta();

                if (reporteCreado != 0) {
                    // Guardar la información del ultimo reporte generado
                    PreferencesReporte.actualizarUltimoReporte(getApplicationContext(), reporteCreado);
                    notificaciones.crearNotificacionNormal(context, CHANNEL_ID,  R.drawable.ic_color_success, "¡Alerta enviada!", "Se generó alerta con folio #" + reporteCreado, ID_SERVICIO_WIDGET_GENERAR_ALERTA);

                    if(ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED){
                        terminarGrabacionAudio();
                        comenzarGrabacionAudio();
                    } else {
                        Log.d(TAG, "No se tienen los permisos de AUDIO");
                    }

                    terminarServicioGPS();
                    comenzarGPS();

                    // Obtener el numero de ciclos para comenzar a tomar las fotografias
                    PreferencesCiclo preferencesCiclo = new PreferencesCiclo();
                    ciclo = preferencesCiclo.obtenerCicloFotografias(getApplicationContext());
                    /*if(ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                        iniciarProcesoFotografias();
                    } else {
                        //Toast.makeText(context, "No tiene permisos de camara", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "No tiene permisos de camara");
                    }*/
                } else {
                    notificaciones.crearNotificacionNormal(getApplicationContext(), CHANNEL_ID, R.drawable.ic_color_error, "¡No se pudo generar la alerta de pánico!", parametros.getString("message", "Sin mensaje"), ID_SERVICIO_WIDGET_CREAR_REPORTE);
                }
            } catch (Exception e){
                Log.e(TAG, "Catch broadscast receiver generar alerta: " + e.getMessage());
            }
        }
    };

    private BroadcastReceiver broadcastReceiverGPSService = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle parametros = intent.getExtras();
            Log.d(TAG, "Los parametros: " + parametros);

            double latitud = parametros.getDouble("latitud", 0.0);
            double longitud = parametros.getDouble("longitud", 0.0);
            String fecha = parametros.getString("fecha", "");
            String padre = parametros.getString("padre");
            int reporte = parametros.getInt("reporteCreado", 0);

            if (latitud != 0.0 && longitud != 0.0 && reporte != 0 && !fecha.equals("")){
                EnviarCoordenadas enviarCoordenadas = new EnviarCoordenadas();
                Boolean hasGPS = enviarCoordenadas.enviarCoordenadas(getApplicationContext(), latitud, longitud, fecha, reporte);
                if(hasGPS){
                    Log.d(TAG, "Coordenadas GPS añadidas con éxito");
                    eliminarEscuchadorGPS();
                    terminarServicioGPS();
                    deboTerminar();
                } else {
                    terminarServicioGPS();
                    deboTerminar();
                    Log.d(TAG, "Error al enviar coordenadas GPS");
                }
            }
        }
    };

    private BroadcastReceiver broadcastReceiverAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle parametros = intent.getExtras();
            boolean termino = parametros.getBoolean("termino");

            Log.d(TAG, "Respondió Servicio");
            if(termino){
                terminarGrabacionAudio();
                boolean terminoAudio = Utilidades.isMyServiceRunning(getApplicationContext(), AudioService.class);
                Log.d(TAG, "TERMINÓ AUDIO SERVICE? " + terminoAudio);
                deboTerminar();
            } else {
                Log.d(TAG, "Termino mal Audio");
                //eliminarEscuchadorAudio();
                deboTerminar();
            }
        }
    };

    public void deboTerminar(){
        Boolean runAudio = Utilidades.isMyServiceRunning(getApplicationContext(), AudioService.class);
        Boolean runGPS = Utilidades.isMyServiceRunning(getApplicationContext(), GPSService.class);
        Boolean runFotograf = Utilidades.isMyServiceRunning(getApplicationContext(), FotografiaService.class);

        if(!runAudio && !runGPS && !runFotograf){
            stopSelf();
            Log.d(TAG, "StopSelf() Servicio Widget");
        } else {
            Log.d(TAG, "Aún no debo terminar. Audio: " + runAudio + " GPS: " + runGPS + " Fotograf: " + runFotograf);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
