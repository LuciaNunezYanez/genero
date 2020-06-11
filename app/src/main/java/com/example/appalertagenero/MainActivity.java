package com.example.appalertagenero;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
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
import android.os.ResultReceiver;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.appalertagenero.Servicios.AudioService;
import com.example.appalertagenero.Servicios.FotografiaService;
import com.example.appalertagenero.Servicios.GPSService;
import com.example.appalertagenero.Utilidades.EnviarAlertaCancelada;
import com.example.appalertagenero.Utilidades.EnviarCoordenadas;
import com.example.appalertagenero.Utilidades.EnviarImagenes;
import com.example.appalertagenero.Utilidades.Notificaciones;
import com.example.appalertagenero.Utilidades.PreferencesReporte;
import com.example.appalertagenero.Utilidades.Utilidades;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;

import static com.example.appalertagenero.Constantes.CHANNEL_ID;
import static com.example.appalertagenero.Constantes.ID_SERVICIO_WIDGET_GENERAR_ALERTA;


public class MainActivity extends AppCompatActivity {

    static String TAG = "Principal";

    // VARIABLES PARA USO DE MULTIMEDIA
    static ImageTraseraResultReceiver resultTReceiver;
    static ImageFrontalResultReceiver resultFReceiver;

    static WeakReference<Context> contextoGlobal;

    public static final int SUCCESS = 1;
    public static final int ERROR = 0;

    static Boolean procesoImagenFrontal = false;
    static Boolean  procesoImageTrasera = false;

    static String IMAGEN_FRONTAL = "Ninguna";
    static String IMAGEN_TRASERA = "Ninguna";

    static String FECHA_FRONTAL = "";
    static String FECHA_TRASERA = "";

    static boolean audio_proceso = false;


    // VARIABLES PARA REPORTE CREADO
    public static int idComercio;
    public static int idUsuario;
    public static int reporteCreado;
    public static boolean btnPresionado = false;

    private static ImageView imgResultado;
    public static Button btnCancelarAlerta;
    static TextView txtResultado;
    static Intent intentGPS;
    ImageButton btnAlerta;
    TextView txtApunta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnAlerta = findViewById(R.id.sendButton);

        btnCancelarAlerta = findViewById(R.id.btnCancelarAlerta);
        imgResultado = findViewById(R.id.imgResultado);
        txtResultado = findViewById(R.id.txtResultado);
        txtApunta = findViewById(R.id.txtApunta);
        contextoGlobal = new WeakReference<>(getApplicationContext());

        // Obtener los IDs del comercio y usuario registrado
        SharedPreferences preferences = getSharedPreferences("Login", Context.MODE_PRIVATE);
        if (preferences.contains("comercio") && preferences.contains("usuario")){
            idComercio = preferences.getInt("comercio" ,0);
            idUsuario = preferences.getInt("usuario", 0);
        } else {
            Intent intent = new Intent(MainActivity.this, RegistroActivity.class);
            startActivity(intent);
            MainActivity.this.finish();
        }
        // MENÚ DE BOTONES FLOTANTES
        final FloatingActionsMenu menuBotones = findViewById(R.id.grupofab);
        FloatingActionButton fabPerfil = findViewById(R.id.fabPerfil);
        FloatingActionButton fabConf = findViewById(R.id.fabConfig);
        FloatingActionButton fabInf = findViewById(R.id.fabInfo);
        FloatingActionButton fabViol = findViewById(R.id.fabViolentometro);

        mostrarResultadoVista(R.drawable.ic_color_info, "¡Genere una alerta de pánico\nal presionar el botón!");

        //Ocultar botón cancelar alerta
        desactivarBotonCancelar();
        //Validar si el botón de cancelar esta activo o no
        Boolean puede = PreferencesReporte.puedeCancelarAlerta(contextoGlobal.get(), System.currentTimeMillis());
        if(puede){
            activarBotonCancelar();
        }
        if(Constantes.URL.equals("http://189.254.158.196:8888")){
            txtApunta.setText("SERVIDOR");
        } else {
            txtApunta.setText("MODO LOCAL");
        }


        fabConf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ConfiguracionActivity.class);
                startActivity(intent);
                menuBotones.collapse();
            }
        });
        fabPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, PerfilActivity.class);
                startActivity(intent);
                menuBotones.collapse();
            }
        });
        fabInf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DirectorioActivity.class);
                startActivity(intent);
                menuBotones.collapse();
            }
        });
        fabViol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ViolentometroActivity.class);
                startActivity(intent);
                menuBotones.collapse();
            }
        });
    }

    public void activarBoton(View view){
        // Validar si se puede generar una nueva alerta
        Boolean puedeEnviar = PreferencesReporte.puedeEnviarReporte(getApplicationContext(), System.currentTimeMillis());

        if(puedeEnviar) {
            mostrarResultadoVista(R.drawable.ic_color_send, "Enviando..");
            PreferencesReporte.guardarReporteInicializado(getApplicationContext());
            enviarAlerta(getApplicationContext(), idComercio, idUsuario);

        } else{
            Log.d(TAG, "NO PUEDE GENERAR REPORTE POR QUE HAY UNO PENDIENTE!!");
        }
    }

    /**********************************************************************************************
     *                                 GENERAR  ALERTA                                             *
     **********************************************************************************************/

    public static void enviarAlerta(final Context context, int idComercio, int idUsuario){
        JsonObjectRequest requestAlerta;
        // + obtenerToken(context)
        String URL = Constantes.URL + "/alerta/";
        Log.d(TAG, "La URL quedó así: " + URL);

        final RequestQueue requestQueue = Volley.newRequestQueue(context);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("idComercio", idComercio);
            jsonObject.put("idUsuario", idUsuario);
            jsonObject.put("fecha" , Utilidades.obtenerFecha());
        }catch (JSONException e){
            e.printStackTrace();
            Log.d(TAG, e.toString());
        }
        final String requestBody = jsonObject.toString();
        requestAlerta = new JsonObjectRequest(Request.Method.POST, URL, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d(TAG, "Response vale: " + response);
                            Boolean ok = response.getBoolean("ok");

                            if(ok){
                                // GUARDAR INFORMACION DEL ULTIMO REPORTE GENERADO
                                reporteCreado = response.getInt("reporteCreado");
                                Notificaciones notificaciones = new Notificaciones();
                                notificaciones.crearNotificacionNormal(context, CHANNEL_ID,  R.drawable.ic_color_success, "¡Alerta enviada!", "Se generó alerta con folio #" + reporteCreado, ID_SERVICIO_WIDGET_GENERAR_ALERTA);

                                mostrarResultadoVista(R.drawable.ic_color_success, "¡Éxito al generar alerta #" + reporteCreado + "!\nEnviando multimedia..");

                                PreferencesReporte.actualizarUltimoReporte(context, reporteCreado);
                                Toast.makeText(context, response.getString("message"), Toast.LENGTH_SHORT).show();
                                btnPresionado = true;

                                // COMENZAR A GENERAR LA MULTIMEDIA
                                registrarEscuchadorGPS(context);

                                if(ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                                    comenzarGPS(context);
                                } else {
                                    //Toast.makeText(context, "No tiene permisos GPS", Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, "No tiene permisos GPS");
                                }

                                if(ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED){
                                    comenzarGrabacionAudio(context);
                                } else {
                                    //Toast.makeText(context, "No tiene permisos de audio", Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, "No tiene permisos de audio");
                                }

                                if(ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                                    iniciarProcesoFotografias(context);
                                } else {
                                    //Toast.makeText(context, "No tiene permisos de camara", Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, "No tiene permisos de camara");
                                }
                            } else {
                                Toast.makeText(context, response.getString("message"), Toast.LENGTH_SHORT).show();
                                Notificaciones notificaciones = new Notificaciones();
                                notificaciones.crearNotificacionNormal(context, CHANNEL_ID,  R.drawable.ic_color_error, "¡No se pudo generar la alerta de pánico!", response.getString("message"), ID_SERVICIO_WIDGET_GENERAR_ALERTA);
                                mostrarResultadoVista(R.drawable.ic_color_error,  response.getString("message"));
                                btnPresionado = false;
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d(TAG, "Algún error con los permisos");
                        }
                        requestQueue.stop();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorResp = "Error #1: Desconocido.";
                if (error instanceof TimeoutError) {
                    errorResp = "Error #1: Se ha agotado el tiempo de espera.";
                    mostrarResultadoVista(R.drawable.ic_color_error, "Se ha agotado el tiempo de espera.");
                } else if (error instanceof NoConnectionError) {
                    errorResp = "Error #1: Sin Conexión.";
                    mostrarResultadoVista(R.drawable.ic_color_error, "Sin Conexión.");
                } else if (error instanceof AuthFailureError) {
                    errorResp = "Error #1: Falló al autenticar.";
                    mostrarResultadoVista(R.drawable.ic_color_error, "Falló al autenticar.");
                } else if (error instanceof ServerError) {
                    errorResp = "Error #1: El servidor no responde.";
                    mostrarResultadoVista(R.drawable.ic_color_error, "El servidor no responde.");
                    Log.d(TAG, error.getMessage());
                } else if (error instanceof NetworkError) {
                    errorResp = "Error #1: Red";
                    mostrarResultadoVista(R.drawable.ic_color_error, "Error de red.");
                } else if (error instanceof ParseError) {
                    errorResp = "Error #1: Parseo";
                    mostrarResultadoVista(R.drawable.ic_color_error, "Error de parseo.");
                }

                Notificaciones notificaciones = new Notificaciones();
                notificaciones.crearNotificacionNormal(context, CHANNEL_ID,  R.drawable.ic_color_error, "¡No se pudo generar la alerta de pánico!", errorResp, ID_SERVICIO_WIDGET_GENERAR_ALERTA);
                Log.d(TAG, errorResp);

                Log.d(TAG, error.toString());
                Toast.makeText(context, errorResp, Toast.LENGTH_LONG).show();
                requestQueue.stop();
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
            @Override
            public byte[] getBody() {
                try {
                    return requestBody == null ? null : requestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Codificación no compatible al intentar obtener los bytes de% s usando %s", requestBody, "utf-8");
                    return null;
                }
            }
        };
        requestQueue.add(requestAlerta);
    }

    /**********************************************************************************************
     *                                     FOTOGRAFIAS                                             *
     **********************************************************************************************/

    public static void iniciarProcesoFotografias(Context context){

        if(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)){
            // INICIAR PROCESO CAMARA FRONTAL
            resultFReceiver = new ImageFrontalResultReceiver(new Handler());
            Intent intentFrontal = new Intent(context, FotografiaService.class);
            intentFrontal.putExtra("reporteCreado", reporteCreado);
            intentFrontal.putExtra("tipoCamara", "frontal");
            intentFrontal.putExtra("receiver", resultFReceiver);
            context.startService(intentFrontal);

        } else if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // INICIAR PROCESO CAMARA TRAERA
            resultTReceiver = new ImageTraseraResultReceiver(new Handler());
            Intent intentTrasera = new Intent(context, FotografiaService.class);
            intentTrasera.putExtra("reporteCreado", reporteCreado);
            intentTrasera.putExtra("tipoCamara", "trasera");
            intentTrasera.putExtra("receiver", resultTReceiver);
            context.startService(intentTrasera);
        } else {
            if(procesoImagenFrontal){
                // ENVIA LA IMAGEN FRONTAL (2)
                Boolean puede = PreferencesReporte.puedeCancelarAlerta(contextoGlobal.get(), System.currentTimeMillis());
                if(puede){
                    activarBotonCancelar();
                }
                Boolean res = EnviarImagenes.enviarImagenFrontal(contextoGlobal.get(), IMAGEN_FRONTAL, FECHA_FRONTAL, reporteCreado);
                Log.d(TAG, "El resultado del envio de frontal es: "+ res);
            } else{
                // TERMINA PROCESO Y NO ENVIA NADA (3)
                finProcesoFotografias();
            }
        }
    }

    public static void finProcesoFotografias(){
        Log.d(TAG, "Termina proceso vacio de fotografias");
        // Al menos guardar las fotografía en el dispositivo
    }

    public static void terminarFotografias(Context context){
        Intent intentFotos = new Intent(context, FotografiaService.class);
        context.stopService(intentFotos);
        Log.d(TAG, "KKKKKKKKKKKKKK");
    }

    /**********************************************************************************************
     *                                        GPS                                                  *
     **********************************************************************************************/
    public static void comenzarGPS(Context context){
        Log.d(TAG, "GPS " + "Comienza GPS");
        intentGPS = new Intent(context, GPSService.class);
        intentGPS.putExtra("reporteGenerado", reporteCreado);
        intentGPS.putExtra("padre", "MainActivity");
        context.startService(intentGPS);
    }

    public static void terminarGPS(Context context){
        Log.d(TAG, "GPS " + "Comienza GPS");
        Intent intentGPS = new Intent(context, GPSService.class);
        context.stopService(intentGPS);
    }

    public static void registrarEscuchadorGPS(Context context){
        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiverGPSService, new IntentFilter("GPSService"));
    }

    public static void eliminarEscuchadorGPS(Context context){
        LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiverGPSService);
    }

    private static BroadcastReceiver broadcastReceiverGPSService = new BroadcastReceiver() {
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
                Boolean hasGPS = enviarCoordenadas.enviarCoordenadas(context, latitud, longitud, fecha, reporte);
                if(hasGPS){
                    Log.d(TAG, "Coordenadas GPS añadidas con éxito");
                    eliminarEscuchadorGPS(context);
                    terminarGPS(context);
                } else {
                    Log.d(TAG, "Error al enviar coordenadas GPS");
                    terminarGPS(context);
                }
            } else if(latitud == 0.0 && longitud == 0.0){
                //Toast.makeText(context, parametros.getString("mensaje"), Toast.LENGTH_SHORT).show();
                terminarGPS(context);
            }
        }
    };

    /**********************************************************************************************
     *                                          AUDIO                                              *
     **********************************************************************************************/

    public static void comenzarGrabacionAudio(Context context){
        Intent intent = new Intent(context, AudioService.class);
        intent.putExtra("nombreAudio", "GrabacionBotonDePanico");
        intent.putExtra("padre", "in");
        intent.putExtra("reporteCreado", reporteCreado);
        audio_proceso = true;
        context.startService(intent);
    }


    /**********************************************************************************************
     *                         RESULT RECEIVERS - FOTOGRAFIAS                                      *
     **********************************************************************************************/

    private static class ImageTraseraResultReceiver extends ResultReceiver {
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

            String message = resultData.getString("mensaje");

            switch (resultCode) {
                case ERROR:
                    //Toast.makeText(contextoGlobal.get(), message, Toast.LENGTH_SHORT).show();
                    break;
                case SUCCESS:
                    Boolean puede = PreferencesReporte.puedeCancelarAlerta(contextoGlobal.get(), System.currentTimeMillis());
                    if (puede){
                        activarBotonCancelar();
                    }
                    if(procesoImagenFrontal){
                        Boolean res = EnviarImagenes.enviarImagenFrontal(contextoGlobal.get(), IMAGEN_FRONTAL, FECHA_FRONTAL, reporteCreado);
                        Log.d(TAG, "El resultado del envio de frontal es: "+ res);
                    }
                    if(procesoImageTrasera){
                        Boolean res = EnviarImagenes.enviarImagenTrasera(contextoGlobal.get(), IMAGEN_TRASERA, FECHA_TRASERA, reporteCreado);
                        Log.d(TAG, "El resultado del envio de trasera es: "+ res);
                    }
                    break;
            }
            super.onReceiveResult(resultCode, resultData);
        }
    }

    private static class ImageFrontalResultReceiver extends ResultReceiver {
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

            String message = resultData.getString("mensaje");

            switch (resultCode) {
                case ERROR:
                    //Toast.makeText(contextoGlobal.get(), message, Toast.LENGTH_SHORT).show();
                    break;
                case SUCCESS:

                    if(contextoGlobal.get().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){

                        // INICIAR PROCESO CAMARA TRAERA
                        resultTReceiver = new ImageTraseraResultReceiver(new Handler());
                        Intent intentTrasera = new Intent( contextoGlobal.get(), FotografiaService.class);
                        intentTrasera.putExtra("reporteCreado", reporteCreado);
                        intentTrasera.putExtra("tipoCamara", "trasera");
                        intentTrasera.putExtra("receiver", resultTReceiver);
                        contextoGlobal.get().startService(intentTrasera);


                    }else {
                        Boolean puede = PreferencesReporte.puedeCancelarAlerta(contextoGlobal.get(), System.currentTimeMillis());
                        if(puede){
                            activarBotonCancelar();
                        }
                        if(procesoImagenFrontal){
                            // ENVIAR LA IMAGEN FRONTAL (5)
                            Boolean res = EnviarImagenes.enviarImagenFrontal(contextoGlobal.get(), IMAGEN_FRONTAL, FECHA_FRONTAL, reporteCreado);
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


    /**********************************************************************************************
     *                                 INTERACCIÓN UI                                              *
     **********************************************************************************************/

    /* Mostrar el resultado de la operación al usuario*/
    public static void mostrarResultadoVista(int imagen, String mensaje){
        imgResultado.setImageResource(imagen);
        txtResultado.setText(mensaje);
    }

    public static void activarBotonCancelar(){
        btnCancelarAlerta.setEnabled(true);
        btnCancelarAlerta.setBackgroundColor(Color.rgb(228, 115,79));
    }

    public static void desactivarBotonCancelar(){
        btnCancelarAlerta.setEnabled(false);
        btnCancelarAlerta.setBackgroundColor(Color.rgb(184, 181, 174));
    }

    // Al cancelar la alerta detener el envío de archivos multimedia?
    public void cancelarAlerta(View view){
        //Cancelar el ultimo reporte encontrado.
        android.content.SharedPreferences preferences = getSharedPreferences("UltimoReporte", Context.MODE_PRIVATE);
        if(preferences.contains("estatusReporte")){
            int ultimoReporte = preferences.getInt("ultimoReporte", 0);
            if (ultimoReporte != 0 ){
                EnviarAlertaCancelada enviarAlertaCancelada = new EnviarAlertaCancelada();
                enviarAlertaCancelada.enviarAlertaCancelada(getApplicationContext(), ultimoReporte, 3);
                mostrarResultadoVista(R.drawable.ic_color_error, "¡La alerta ha sido cancelada!");
                Toast.makeText(contextoGlobal.get(), "Cancelando alerta..", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(contextoGlobal.get(), "¡El ultimo reporte es inválido!", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG,"Guardó datos ");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG,"Recuperó datos ");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        terminarFotografias(getApplication());

        /*getApplication().stopService(new Intent(getApplication(), AudioService.class));

                Intent intent = new Intent(getApplication(), AudioService.class);
                intent.putExtra("nombreAudio", "GrabacionBotonDePanico");
                intent.putExtra("padre", "out");
                intent.putExtra("reporteCreado", reporteCreado);
                getApplication().startService(intent);*/
        //Toast.makeText(contextoGlobal.get(), "¡ON DESTROY MAIN!", Toast.LENGTH_LONG).show();


    }
}
