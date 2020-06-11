package com.example.appalertagenero.Servicios;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

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
import com.example.appalertagenero.Constantes;
import com.example.appalertagenero.Utilidades.PreferencesReporte;
import com.example.appalertagenero.Utilidades.Utilidades;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;

import static com.example.appalertagenero.Utilidades.PreferencesComercio.obtenerToken;

public class GenerarAlertaService extends Service {

    static String TAG = "Notificacion";
    // ===============================
    // VARIABLES GENERALES
    // ===============================
    private int idComercio;
    private int idUsuario;
    private String fecha;
    public static int reporteCreado;
    public static Boolean alertaRecibida;

    public static WeakReference<Context> contextoGlobal;
    public static Intent intentGlobal;

    public GenerarAlertaService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("onBind no implementado");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        contextoGlobal = new WeakReference<>(getApplicationContext());
        intentGlobal = intent;

        Log.d("PRUEBA", "Entro a generarAlertaService()");

        if(intent == null){
            Log.d(TAG, "Intent NULL");
            darResultados(getApplicationContext(), 0, false, false, false, "Intent NULL");
            stopSelf();
        } else {
            Log.d(TAG, "Intent no NULL");
        }

        // Recuperar valores de entrada
        try{
            idComercio = intent.getIntExtra("comercio", 0);
            idUsuario = intent.getIntExtra("usuario", 0);
            // sala = intent.getStringExtra("sala");
            fecha = intent.getStringExtra("fecha");
        }catch ( Exception e ){
            Log.d(TAG, "Debe regresar que no se encontrarón los datos parametros de entrada");
            darResultados(getApplicationContext(), 0, false, false, false, "Los datos del grupo son incorrectos");
            stopSelf();
        }

        if (idComercio != 0 && idUsuario != 0){
            Log.d(TAG, "Recibí:" + " Comercio "+ idComercio + " Usuario " + idUsuario + " Fecha: " + fecha);
            generarReporte();

        } else {
            Log.d(TAG, "No venian los datos del comercio: " + idComercio + ", user: " + idUsuario);
            darResultados(getApplicationContext(),0, false, false, false, "Los datos del grupo son incorrectos");
            stopSelf();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void generarReporte(){
        enviarAlerta(getApplicationContext(), idComercio, idUsuario);
    }

    /*public static void stop(){
        contextoGlobal.get().stopService(intentGlobal);
    }*/

    // ************************************************
    // ALERTA ALERTA ALERTA ALERTA ALERTA ALERTA ALERTA
    // ************************************************
    public static void enviarAlerta(final Context context, int idComercio, int idUsuario){
        JsonObjectRequest requestAlerta;
        String URL = Constantes.URL + "/alerta/"+ obtenerToken(context);
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
                                reporteCreado = response.getInt("reporteCreado");
                                Log.d(TAG, "Se creó el reporte con folio #" + reporteCreado);

                                // GUARDAR INFORMACION DEL ULTIMO REPORTE GENERADO
                                PreferencesReporte.actualizarUltimoReporte(context, reporteCreado);

                                alertaRecibida = true;
                                darResultados(context, reporteCreado, true, true, true, response.getString("message"));
                            } else {
                                darResultados(context, 0, false, true, true, response.getString("message"));
                                Log.d(TAG, "El reporte no ha sido creado");
                                alertaRecibida = false;
                                reporteCreado = 0;
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d(TAG, "Error al obtener valores del JSON de respuesta");
                        }
                        requestQueue.stop();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorResp = "Error #6: Desconocido";

                if (error instanceof TimeoutError) {
                    errorResp = "Error #6: Verifique su conexión";
                } else if (error instanceof NoConnectionError) {
                    errorResp = "Error #6: Sin conexión con el servidor";
                } else if (error instanceof AuthFailureError) {
                    errorResp = "Error #6: Fallo al autenticar";
                } else if (error instanceof ServerError) {
                    errorResp = "Error #6: Servidor";
                } else if (error instanceof NetworkError) {
                    errorResp = "Error #6: Red";
                } else if (error instanceof ParseError) {
                    errorResp = "Error #6: Parseo";
                }
                Log.e(TAG, errorResp);
                darResultados(context, 0, false, false, false, errorResp);
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




    @Override
    public void onDestroy() {
        super.onDestroy();
        System.err.println("on Destroy > is Finishing ");
        stopSelf();
    }

    private static void darResultados(Context context, int reporteCreado, boolean envioArchivos, boolean respondioServer, boolean datosComercio, String message){
        Intent intent = new Intent("generarAlertaService");
        intent.putExtra("reporteCreado", reporteCreado);
        intent.putExtra("envioArchivos",  envioArchivos);
        intent.putExtra("respondioServer", respondioServer);
        intent.putExtra("datosComercio", datosComercio);
        intent.putExtra("message", message);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

        //stopSelf();
    }
}