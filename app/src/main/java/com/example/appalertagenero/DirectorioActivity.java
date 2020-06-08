package com.example.appalertagenero;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.appalertagenero.Clases.AdaptadorDirectorio;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DirectorioActivity extends AppCompatActivity {

    static String TAG = "Directorio";
    static ListView lista;
    /*static String[][] datos_directorio = {
            {"Atención a Mujeres victimas", "Calle Cipres #123 Col Diana Laura. Victoria de Durango, Dgo. Entre Regato y Pasteur", "6189999999"},
            {"Atención a Mujeres victimas", "Calle Cipres #123 Col Diana Laura. Victoria de Durango, Dgo. Entre Regato y Pasteur", "6189999999"},
            {"Atención a Mujeres victimas", "Calle Cipres #123 Col Diana Laura. Victoria de Durango, Dgo. Entre Regato y Pasteur", "6189999999"},
            {"Atención a Mujeres victimas", "Calle Cipres #123 Col Diana Laura. Victoria de Durango, Dgo. Entre Regato y Pasteur", "6189999999"},
            {"Atención a Mujeres victimas", "Calle Cipres #123 Col Diana Laura. Victoria de Durango, Dgo. Entre Regato y Pasteur", "6189999999"},
            {"Atención a Mujeres victimas", "Calle Cipres #123 Col Diana Laura. Victoria de Durango, Dgo. Entre Regato y Pasteur", "6189999999"}
    };*/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directorio);
        lista = findViewById(R.id.listDirectorio);
        getDirectorio(getApplicationContext(), "genero");
    }

    public static void getDirectorio(final Context context, String filtro){

        StringRequest requestGetDirectorio;
        String URL = Constantes.URL + "/directorio/" + filtro;
        Log.d(TAG, URL);

        final RequestQueue requestQueue = Volley.newRequestQueue(context);

        requestGetDirectorio = new StringRequest(Request.Method.GET, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONObject object_response = new JSONObject(response);
                    Boolean ok = object_response.getBoolean("ok");
                    if(ok){
                        JSONArray object_directorio = object_response.getJSONArray("directorio");
                        String datos_DB[][] = new String[object_directorio.length()][3];
                        for (int e = 0; e < object_directorio.length(); e ++){
                            try {
                                JSONObject centro =  new JSONObject( object_directorio.getString(e));
                                try{
                                    datos_DB[e][0] = centro.getString("nombre_direct");
                                } catch (Exception p ){
                                    datos_DB[e][0] = "CENTRO DESCONOCIDO.";
                                }
                                try{
                                    String direccion_completa =
                                                centro.getString("calle") +
                                                " #" + centro.getString("numero") +
                                                ", " + centro.getString("colonia") +
                                                "\nC.P. " + centro.getString("cp") +
                                                " " + centro.getString("nombre_localidad") +
                                                ", " + centro.getString("abrev") +
                                                "\nEntre " + centro.getString("entre_calle_1") +
                                                " y " + centro.getString("entre_calle_2");
                                    datos_DB[e][1] = direccion_completa;
                                } catch (Exception p){
                                    datos_DB[e][1] = "Dirección incorrecta.";
                                }
                                try{
                                    datos_DB[e][2] = centro.getString("telefonos");
                                } catch (Exception p){
                                    datos_DB[e][2] = "Teléfono desconocido.";
                                }

                            } catch (Exception i){
                                datos_DB[e][0] = "Centro desconocido.";
                                datos_DB[e][1] = "Dirección incorrecta.";
                                datos_DB[e][2] = "Teléfono incorrecto.";
                            }
                        }
                        lista.setAdapter(new AdaptadorDirectorio(context, datos_DB));
                    }
                }
                catch(JSONException e){
                    Toast.makeText(context, "Error"+e.toString(), Toast.LENGTH_SHORT).show();
                }
                requestQueue.stop();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorResp = "Error #9: " + R.string.error_desconocido;

                if (error instanceof TimeoutError) {
                    errorResp = "Error #10: " + R.string.error_tiempo_agotado;
                } else if (error instanceof NoConnectionError) {
                    errorResp = "Error #10: " + R.string.error_sin_conexion;
                } else if (error instanceof AuthFailureError) {
                    errorResp = "Error #10: " + R.string.error_fallo_autenticar;
                } else if (error instanceof ServerError) {
                    errorResp = "Error #10: " + R.string.error_servidor;
                } else if (error instanceof NetworkError) {
                    errorResp = "Error #10: " + R.string.error_red;
                } else if (error instanceof ParseError) {
                    errorResp = "Error #10: " + R.string.error_parseo;
                }

                // Toast.makeText(context, errorResp, Toast.LENGTH_SHORT).show();
                Log.e(TAG, errorResp);
                requestQueue.stop();
            }
        });
        requestQueue.add(requestGetDirectorio);
    }


    private void initList(){


    }

    public static HashMap<String, String> createEmployee(String name, String number){
        HashMap<String, String> employeeNameNo = new HashMap<String, String>();
        employeeNameNo.put(name, number);
        return employeeNameNo;
    }

}
