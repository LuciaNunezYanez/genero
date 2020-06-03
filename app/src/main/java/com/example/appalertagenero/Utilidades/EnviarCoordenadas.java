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

public class EnviarCoordenadas {

    private static String TAG = "Coordenadas";

    public static Boolean enviarCoordenadas(final Context context, Double latitud, Double longitud, String fecha, int reporteCreado){
        StringRequest requestCoordenadas;
        Boolean seEnviaron = false;

        if(reporteCreado >=1 ){
            // COMIENZA HILO PARA ENVIAR IMAGEN FRONTAL
            Log.d(TAG, "Comienza hilo para enviar coordenadas GPS ");

            String URL = Constantes.URL + "/coordenadas/" + reporteCreado;

            final RequestQueue requestQueue = Volley.newRequestQueue(context);
            JSONObject jsonObjectBody = new JSONObject();
            try {
                jsonObjectBody.put("lat_coord_reporte", latitud);
                jsonObjectBody.put("lng_coord_reporte", longitud);
                jsonObjectBody.put("fecha_coord_reporte", fecha);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, e.toString());
            }
            final String requestBody = jsonObjectBody.toString();
            requestCoordenadas = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i(TAG, "La respuesta de las coordenas es: " + response);
                    int valorTrue = response.indexOf(":true,");
                    switch (valorTrue){
                        case -1:
                            Log.d(TAG, "No viene TRUE en la respuesta");
                            int valorFalse = response.indexOf(":false,");
                            switch (valorFalse){
                                case -1:
                                    Log.d(TAG, "No viene TRUE ni FALSE en la respuesta");
                                    break;
                                default:
                                    Log.d(TAG, "Viene FALSE, por lo que no se agregaron coordenadas");
                                    break;
                            }
                            break;
                        default:
                            Log.d(TAG, "Si viene la cadena en: " + valorTrue);
                            break;
                    }
                    // Detener proceso una vez enviadas las coordenadas
                    requestQueue.stop();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    String errorResp = "Error #3: " + R.string.error_desconocido;

                    if (error instanceof TimeoutError) {
                        errorResp = "Error #3: " + R.string.error_tiempo_agotado;
                    } else if (error instanceof NoConnectionError) {
                        errorResp = "Error #3: " + R.string.error_sin_conexion;
                    } else if (error instanceof AuthFailureError) {
                        errorResp = "Error #3: " + R.string.error_fallo_autenticar;
                    } else if (error instanceof ServerError) {
                        errorResp = "Error #3: " + R.string.error_servidor;
                    } else if (error instanceof NetworkError) {
                        errorResp = "Error #3: " + R.string.error_red;
                    } else if (error instanceof ParseError) {
                        errorResp = "Error #3: " + R.string.error_parseo;
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

            // Código de prueba. Puede generar errores
            // requestCoordenadas.setRetryPolicy(new DefaultRetryPolicy(5000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            requestQueue.add(requestCoordenadas);
            seEnviaron = true;

        } else if (reporteCreado == -1){
            seEnviaron = false;

        } else {
            Log.d(TAG, "Se agotó el tiempo de espera para coordenadas!!! ");
            seEnviaron = false;

        }
        return seEnviaron;
    }
}
