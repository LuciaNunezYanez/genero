package com.example.appalertagenero.Utilidades;

import android.content.Context;
import android.util.Log;
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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.appalertagenero.Constantes;
import com.example.appalertagenero.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class EnviarImagenes {

    static String TAG = "Imagenes";

    public static Boolean enviarImagenFrontal(final Context context, String IMAGEN_FRONTAL, String FECHA_FRONTAL, int reporteCreado){
        //Log.d(TAG, "Enviare imagen frontal" + IMAGEN_FRONTAL.length());

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e){
            Log.d(TAG, "Excepcion enviar imagenes hilo");
        }

        StringRequest requestFrontal;
        if(reporteCreado >=1 ){

            // COMIENZA HILO PARA ENVIAR IMAGEN FRONTAL
            Log.d(TAG, "Comienza hilo para enviar imagen frontal");

            // Cuerpo de la petición
            String archivo = IMAGEN_FRONTAL;
            String URL = Constantes.URL + "/upload/imagenes/" + reporteCreado;

            final RequestQueue requestQueue = Volley.newRequestQueue(context);
            JSONObject jsonObjectBody = new JSONObject();
            try {
                jsonObjectBody.put("fecha", FECHA_FRONTAL);
                jsonObjectBody.put("imagen", archivo);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, e.toString());
            }

            final String requestBody = jsonObjectBody.toString();
            requestFrontal = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    // RECIBIR LA RESPUESTA DEL WEB SERVICE CUANDO TOD ESTA CORRECTO
                    Log.i(TAG, "La respuesta de imagen frontal es: " + response);
                    requestQueue.stop();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    String errorResp = "Error #4: Desconocido";

                    if (error instanceof TimeoutError) {
                        errorResp = "Error #4: Timeout";
                    } else if (error instanceof NoConnectionError) {
                        errorResp = "Error #4: Sin Conexión";
                    } else if (error instanceof AuthFailureError) {
                        errorResp = "Error #4: Fallo al autenticar";
                    } else if (error instanceof ServerError) {
                        errorResp = "Error #4: Servidor";
                    } else if (error instanceof NetworkError) {
                        errorResp = "Error #4: Red";
                    } else if (error instanceof ParseError) {
                        errorResp = "Error #4: Parseo";
                    }
                    Toast.makeText(context, errorResp, Toast.LENGTH_SHORT).show();
                    requestQueue.stop();
                    Log.e(TAG, error.toString());
                    Log.e(TAG, errorResp);
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return requestBody == null ? null : requestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Codificación no compatible al intentar obtener los bytes de% s usando %s", requestBody, "utf-8");
                        return null;
                    }
                }
            };
            requestQueue.add(requestFrontal);

            return true;


        } else if (reporteCreado == -1){
            return false;

        } else {
            Log.d(TAG, "Se agotó el tiempo de espera frontal!!! ");
            return false;

        }
    }

    public static Boolean enviarImagenTrasera(final Context context, String IMAGEN_TRASERA, String FECHA_TRASERA, int reporteCreado){

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e){
            Log.d(TAG, "Excepcion enviar imagenes hilo");
        }


        StringRequest requestTrasera;

        if(reporteCreado >=1 ){
            // COMIENZA HILO PARA ENVIAR IMAGEN FRONTAL
            Log.d(TAG, "Comienza hilo para enviar imagen trasera");


            // Cuerpo de la petición
            String archivo = IMAGEN_TRASERA;
            String URL = Constantes.URL + "/upload/imagenes/" + reporteCreado;

            final RequestQueue requestQueue = Volley.newRequestQueue(context);
            JSONObject jsonObjectBody = new JSONObject();
            try {
                jsonObjectBody.put("fecha", FECHA_TRASERA);
                jsonObjectBody.put("imagen", archivo);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, e.toString());
            }

            final String requestBody = jsonObjectBody.toString();
            requestTrasera = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    // RECIBIR LA RESPUESTA DEL WEB SERVICE CUANDO TOD ESTA CORRECTO

                    Log.i(TAG, "La respuesta de imagen trasera es: " + response);
                    requestQueue.stop();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    String errorResp = "Error #5: Desconocido";

                    if (error instanceof TimeoutError) {
                        errorResp = "Error #5: Timeout";
                    } else if (error instanceof NoConnectionError) {
                        errorResp = "Error #5: Sin Conexión";
                    } else if (error instanceof AuthFailureError) {
                        errorResp = "Error #5: Fallo al autenticar";
                    } else if (error instanceof ServerError) {
                        errorResp = "Error #5: Servidor";
                    } else if (error instanceof NetworkError) {
                        errorResp = "Error #5: Red";
                    } else if (error instanceof ParseError) {
                        errorResp = "Error #5: Parseo";
                    }
                    Toast.makeText(context, errorResp, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, errorResp);
                    requestQueue.stop();
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return requestBody == null ? null : requestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Codificación no compatible al intentar obtener los bytes de% s usando %s", requestBody, "utf-8");
                        return null;
                    }
                }
            };
            requestQueue.add(requestTrasera);
            return true;

        } else if (reporteCreado == -1){
            return false;

        } else {
            Log.d(TAG, "Se agotó el tiempo de espera trasera!!! ");
            return false;

        }
    }
}
