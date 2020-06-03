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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DirectorioActivity extends AppCompatActivity {

    static String TAG = "Directorio";
    static ListView lista;
    static String[][] datos_directorio = {
            {"Atención a Mujeres victimas", "Calle Cipres #123 Col Diana Laura. Victoria de Durango, Dgo. Entre Regato y Pasteur"},
            {"Atención a Mujeres victimas", "Calle Cipres #123 Col Diana Laura. Victoria de Durango, Dgo. Entre Regato y Pasteur"},
            {"Atención a Mujeres victimas", "Calle Cipres #123 Col Diana Laura. Victoria de Durango, Dgo. Entre Regato y Pasteur"},
            {"Atención a Mujeres victimas", "Calle Cipres #123 Col Diana Laura. Victoria de Durango, Dgo. Entre Regato y Pasteur"},
            {"Atención a Mujeres victimas", "Calle Cipres #123 Col Diana Laura. Victoria de Durango, Dgo. Entre Regato y Pasteur"},
            {"Atención a Mujeres victimas", "Calle Cipres #123 Col Diana Laura. Victoria de Durango, Dgo. Entre Regato y Pasteur"},
            {"Atención a Mujeres victimas", "Calle Cipres #123 Col Diana Laura. Victoria de Durango, Dgo. Entre Regato y Pasteur"},
            {"Atención a Mujeres victimas", "Calle Cipres #123 Col Diana Laura. Victoria de Durango, Dgo. Entre Regato y Pasteur"},
    };

    static String[][] datos_DB = {

    };
    static ArrayList<String> directorio = new ArrayList<>();
    static List<Map<String,String>> employeeList = new ArrayList<Map<String,String>>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directorio);
        lista = findViewById(R.id.listDirectorio);
        getDirectorio(getApplicationContext(), "alerta de genero");
    }

    public static void getDirectorio(final Context context, String filtro){

        StringRequest requestGetDirectorio;
        String URL = Constantes.URL + "/directorio/" + filtro;

        final RequestQueue requestQueue = Volley.newRequestQueue(context);

        requestGetDirectorio = new StringRequest(Request.Method.GET, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i(TAG, "La respuesta al obtener directorio: " + response);

                try{
                    JSONObject jsonResponse = new JSONObject(response);
                    /*JSONArray jsonMainNode = jsonResponse.optJSONArray("employee");

                    for(int i = 0; i<jsonMainNode.length();i++){
                        JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
                        String name = jsonChildNode.optString("nombre_direct");
                        String direccion = jsonChildNode.optString("id_direccion");
                        String outPut = name + "-" +direccion;
                        employeeList.add(createEmployee("directorio", outPut));
                    }*/
                }
                catch(JSONException e){
                    Toast.makeText(context, "Error"+e.toString(), Toast.LENGTH_SHORT).show();
                }

                /*try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray ja = jsonObject.getJSONArray("directorio");
                    JSONObject jo;
                    directorio.clear();


                    for (int i = 0; i < ja.length(); i++){
                        jo = ja.getJSONObject(i);
                        Iterator<String> keys = jo.keys();
                        while( keys.hasNext()){
                            String keyName = keys.next();

                        }


                        Log.d(TAG,"XXX- " + ja.getString(i));
                    }

                } catch (JSONException e) {
                    Log.d(TAG, "Error parse");
                    e.printStackTrace();
                }
                */

                lista.setAdapter(new AdaptadorDirectorio(context, datos_directorio));
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
        /*{
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
        };*/
        requestQueue.add(requestGetDirectorio);
        //return seCancelo;
    }


    private void initList(){


    }

    public static HashMap<String, String> createEmployee(String name, String number){
        HashMap<String, String> employeeNameNo = new HashMap<String, String>();
        employeeNameNo.put(name, number);
        return employeeNameNo;
    }

}
