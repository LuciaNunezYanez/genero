package com.c5durango.alertagenero.Utilidades;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.c5durango.alertagenero.Constantes;

import org.json.JSONException;
import org.json.JSONObject;

public class EnviarAlertaCancelada {
    static String TAG = "AlertaCancelada";

    public static void enviarAlertaCancelada(final Context context, int id_reporte_cancelar, int nuevo_estatus){
        StringRequest requestAlertaCancelada;
        String URL = Constantes.URL + "/activaciones/" + id_reporte_cancelar;

        final RequestQueue requestQueue = Volley.newRequestQueue(context);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("estatus", nuevo_estatus);
        }catch (JSONException e){
            e.printStackTrace();
            Log.d(TAG, e.toString());
        }
        requestAlertaCancelada = new StringRequest(Request.Method.PUT, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i(TAG, "La respuesta al cancelar reporte es: " + response);
                requestQueue.stop();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorResp = "Error #9: " + Utilidades.tipoErrorVolley(error);
                Log.e(TAG, errorResp);
                requestQueue.stop();
            }
        });
        requestQueue.add(requestAlertaCancelada);
        //return seCancelo;
    }
}
