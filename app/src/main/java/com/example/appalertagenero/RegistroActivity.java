package com.example.appalertagenero;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.appalertagenero.Utilidades.Utilidades;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Pattern;

public class RegistroActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    // Variables principales
    private Boolean tieneAcceso = false;

    // Components Layout Personales
    LinearLayout linearPersonales;
    EditText txtCorreo, txtNombres, txtPaterno, txtMaterno, dateNacimiento, areaPadecimientos, txtTelefonoM, areaAlergias;
    Spinner spSexo, spSangre;
    Button btnSiguiente, btnFechaNac;

    // Components Layout Domicilio
    LinearLayout linearDomicilio;
    EditText txtCalle, txtNumeroExt, txtColonia, txtCodigoP, txtCalle1, txtCalle2, areaReferencia;
    static Spinner spEstado, spMunicipio, spLocalidad;
    Button btnFinalizar, btnRegresar;

    String stCorreo, stNombres, stPaterno, stMaterno, stNacimiento, stPadecimientos, stTelefonoM, stAlergias, stSexo, stSangre;
    String stCalle, stNumeroExt, stColonia, stCalle1, stCalle2, stReferencia, stEstado, stMunicipio, stLocalidad;
    int codigoP;

    String sexo [] = { "Seleccionar", "Femenino", "Masculino", "Desconocido" };
    String sangre [] = { "Seleccionar", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-", "No sé" };
    String estado [] = { "Durango" };

    // Apoyo a Spinners Dirección
    static int id_loc_select = 0;
    static List<String> misIDsMunicipios = new ArrayList<>();
    static List<String> misMunicipios = new ArrayList<>();
    static List<String> misIDsLocalidades = new ArrayList<>();
    static List<String> misLocalidades = new ArrayList<>();

    static String TAG = "Alerta de género";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        linearPersonales = findViewById(R.id.linearPersonales);
        linearDomicilio = findViewById(R.id.linearDomiclio);

        // Componentes
        txtCorreo = findViewById(R.id.txtCorreo);
        txtNombres = findViewById(R.id.txtNombres);
        txtPaterno = findViewById(R.id.txtPaterno);
        txtMaterno = findViewById(R.id.txtMaterno);
        dateNacimiento = findViewById(R.id.dateNacimiento);
        areaPadecimientos = findViewById(R.id.areaPadecimientos);
        txtTelefonoM = findViewById(R.id.txtTelefono);
        areaAlergias = findViewById(R.id.areaAlergias);
        spSexo = findViewById(R.id.spinnerSexo);
        spSexo.setAdapter( new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_selectable_list_item, sexo));
        spSangre = findViewById(R.id.spinnerTipoSangre);
        spSangre.setAdapter( new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_selectable_list_item, sangre));

        txtCalle = findViewById(R.id.txtCalle);
        txtNumeroExt = findViewById(R.id.txtNumeroExt);
        txtColonia = findViewById(R.id.txtColonia);
        txtCodigoP = findViewById(R.id.txtCodigoP);
        txtCalle1 = findViewById(R.id.txtCalle1);
        txtCalle2 = findViewById(R.id.txtCalle2);
        areaReferencia = findViewById(R.id.areaReferencia);
        spEstado = findViewById(R.id.spinnerEstado);
        spEstado.setAdapter( new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_selectable_list_item, estado));
        spMunicipio = findViewById(R.id.spinnerMunicipio);
        spMunicipio.setAdapter( new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_selectable_list_item, misMunicipios));
        spLocalidad = findViewById(R.id.spinnerLocalidad);
        spLocalidad.setAdapter( new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_selectable_list_item, misLocalidades));

        // Botones
        btnSiguiente = findViewById(R.id.btnSiguiente);
        btnFinalizar = findViewById(R.id.btnFinalizar);
        btnRegresar = findViewById(R.id.btnRegresar);
        btnFechaNac = findViewById(R.id.btnFechaNac);

        btnSiguiente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stCorreo = txtCorreo.getText().toString();
                stNombres = txtNombres.getText().toString();
                stPaterno = txtPaterno.getText().toString();
                stMaterno = txtMaterno.getText().toString();
                stNacimiento = dateNacimiento.getText().toString();
                stPadecimientos = areaPadecimientos.getText().toString();
                stTelefonoM = txtTelefonoM.getText().toString();
                stAlergias = areaAlergias.getText().toString();
                String s = spSexo.getSelectedItem().toString();
                if(s.equals("Femenino"))
                    stSexo = "F";
                if(s.equals("Masculino"))
                    stSexo = "M";
                if(s.equals("Desconocido"))
                    stSexo = "D";
                if(s.equals("Seleccionar"))
                    stSexo = "";

                stSangre = spSangre.getSelectedItem().toString();

                //Toast.makeText(getApplicationContext(), stNombres + " " + stPaterno + " " + stMaterno + " " + stNacimiento + " " +
                  //      stPadecimientos + " " + stTelefonoM + " " + stAlergias + " " + stSexo + " " + stSangre, Toast.LENGTH_LONG).show();

                if(!Utilidades.validEmail(stCorreo)) {
                    Toast.makeText(getApplicationContext(), "Correo electrónico inválido", Toast.LENGTH_LONG).show();

                } else if( stNombres.length() < 3 || stPaterno.length() < 3) {
                    Toast.makeText(getApplicationContext(), "Nombre(s) o apellido paterno inválido", Toast.LENGTH_LONG).show();

                } else if(stSexo.equals("")) {
                    Toast.makeText(getApplicationContext(), "Seleccione sexo por favor", Toast.LENGTH_LONG).show();

                } else if(stTelefonoM.length() != 10 || !Utilidades.validPhone(stTelefonoM)) {

                    try {

                        int tel = Integer.parseInt(stTelefonoM);
                        Toast.makeText(getApplicationContext(), "Teléfono inválido, solo 10 digitos", Toast.LENGTH_LONG).show();

                    } catch (Exception e){
                        Toast.makeText(getApplicationContext(), "No son puros numeros", Toast.LENGTH_LONG).show();
                    }

                } else {
                    linearPersonales.setVisibility(View.GONE);
                    linearDomicilio.setVisibility(View.VISIBLE);
                    // Toast.makeText(getApplicationContext(), "Datos personales completos", Toast.LENGTH_LONG).show();
                }

            }
        });

        btnRegresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                linearPersonales.setVisibility(View.VISIBLE);
                linearDomicilio.setVisibility(View.GONE);
            }
        });

        btnFinalizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stCalle = txtCalle.getText().toString();
                stNumeroExt = txtNumeroExt.getText().toString();
                stColonia = txtColonia.getText().toString();
                codigoP = Integer.parseInt(txtCodigoP.getText().toString());
                stCalle1 = txtCalle1.getText().toString();
                stCalle2 = txtCalle2.getText().toString();
                stReferencia = areaReferencia.getText().toString();
                stEstado = spEstado.getSelectedItem().toString();
                stMunicipio = spMunicipio.getSelectedItem().toString();
                stLocalidad = spLocalidad.getSelectedItem().toString();

                // Toast.makeText(getApplicationContext(), "FIN; " + stCalle + " " + stNumeroExt + " " + stColonia + " " + stCodigoP + " " + stCalle1 + " " +
                //      stCalle2 + " " + stReferencia + " " + stEstado +" " + stMunicipio + " " + stLocalidad, Toast.LENGTH_LONG).show();

                if(true){
                    crearUsuario();
                } else {
                    Toast.makeText(getApplicationContext(), "Datos de domicilio incompletos", Toast.LENGTH_LONG).show();
                }

            }
        });

        btnFechaNac.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mostrarDatePicker();
            }
        });

        spMunicipio.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?>arg0, View view, int arg2, long arg3) {
                misLocalidades.clear();
                misIDsLocalidades.clear();
                spLocalidad.setAdapter( new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_selectable_list_item, misLocalidades));
                getLocalidades(getApplicationContext(), Integer.parseInt(misIDsMunicipios.get(arg2)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                id_loc_select = 0;
            }
        });

        spLocalidad.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                id_loc_select = Integer.parseInt(misIDsLocalidades.get(i));
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                id_loc_select = 0;
            }
        });

        getMunicipios(getApplicationContext(), 10); // 10: Durango

    }

    public void crearUsuario(){
        final RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());

        //1578347038755
        String URL = Constantes.URL + "/registroalertagenero";

        JSONObject jsonObjectBody = new JSONObject();
        try {
            // Enviar todos los datos del usuario y dirección
            jsonObjectBody.put("calle", stCalle);
            jsonObjectBody.put("numero", stNumeroExt);
            jsonObjectBody.put("colonia", stColonia);
            jsonObjectBody.put("cp", codigoP);
            jsonObjectBody.put("entre_calle_1", stCalle1);
            jsonObjectBody.put("entre_calle_2", stCalle2);
            jsonObjectBody.put("fachada", stReferencia);
            jsonObjectBody.put("id_localidad", id_loc_select);
            jsonObjectBody.put("lat_dir", 0);
            jsonObjectBody.put("lgn_dir", 0);

            jsonObjectBody.put("nombres_usuarios_app", stNombres);
            jsonObjectBody.put("apell_pat", stPaterno);
            jsonObjectBody.put("apell_mat", stMaterno);
            jsonObjectBody.put("fecha_nacimiento", stNacimiento);
            jsonObjectBody.put("sexo_app", stSexo);
            jsonObjectBody.put("padecimientos", stPadecimientos);
            jsonObjectBody.put("tel_movil", stTelefonoM);
            jsonObjectBody.put("alergias", stAlergias);
            jsonObjectBody.put("tipo_sangre", stSangre);
            jsonObjectBody.put("estatus_usuario", 1);
            jsonObjectBody.put("correo_usuario", stCorreo);
            jsonObjectBody.put("id_grupo", 2); // 2 = Alerta de genero

            jsonObjectBody.put("id_comercio", 1); // 1 = Comercio con nombre alerta de género

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }
        Log.d(TAG, URL);

        final String requestBody = jsonObjectBody.toString();
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.POST, URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // display response
                        Log.d(TAG, "Respuesta:" + response.toString());

                        String json = response.toString();
                        JSONObject object = null;
                        JSONObject object_resultado = null;
                        try {
                            object = new JSONObject(json);
                            Boolean ok = object.getBoolean("ok");
                            if (ok) {
                                Log.d(TAG, "Ok = TRUE");
                                /*if (object.has("resultado")) {
                                    final String resultado = object.getString("resultado");
                                    if (resultado.equals("Código de activación abierto con éxito")) {
                                        Toast.makeText(getApplicationContext(), "¡BIENVENIDO! \n" + resultado, Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(getApplicationContext(), resultado, Toast.LENGTH_LONG).show();
                                    }
                                }*/

                                /*String token = "S";
                                if(object.has("token")){
                                    token = object.getString("token");
                                }*/

                                if (object.has("resultado")) {
                                    Log.d(TAG, "Resultado = TRUE");
                                    // Obtener cada dato de comercio
                                    object_resultado = object.optJSONObject("resultado");

                                    int id_comercio = object_resultado.getInt("idComercio");
                                    int id_usuario = object_resultado.getInt("idUsuario");
                                    int id_direccion = object_resultado.getInt("idDireccion");
                                    Log.d(TAG, id_comercio + "/" + id_usuario + "/" + id_direccion);

                                    if(object_resultado.has("resultado")){
                                        Log.d(TAG, "chalalala, " + object_resultado);

                                        Log.d(TAG, object_resultado.getInt("resultado") + " = RES");
                                        if(object_resultado.getInt("resultado") == 1){
                                            Log.d(TAG, "Resultado válido");
                                            Toast.makeText(getApplicationContext(), object_resultado.getString("mensage"), Toast.LENGTH_LONG).show();

                                            // Guardar la informacion modo local
                                            if (id_comercio == 1) {
                                                Boolean resp = com.example.appalertagenero.Utilidades.PreferencesComercio.guardarDatosComercio(getApplicationContext(),
                                                        id_comercio,
                                                        id_direccion,
                                                        0,
                                                        "Alerta de género",
                                                        "",
                                                        "",
                                                        "",
                                                        "",
                                                        stCalle,
                                                        stNumeroExt,
                                                        stColonia,
                                                        codigoP,
                                                        stCalle1,
                                                        stCalle2,
                                                        stReferencia,
                                                        id_loc_select,
                                                        stLocalidad,
                                                        stMunicipio,
                                                        stEstado,
                                                        id_usuario,
                                                        stNombres,
                                                        stPaterno,
                                                        stMaterno,
                                                        stNacimiento,
                                                        stSexo,
                                                        stPadecimientos,
                                                        stTelefonoM,
                                                        stAlergias,
                                                        stSangre,
                                                        stCorreo,
                                                        "");

                                                Log.d(TAG, "La respuesta al guardar es: " + resp);
                                                if (resp) {
                                                    tieneAcceso = true;
                                                    activarPermisoAlmacWrite();
                                                } else {
                                                    Log.d(TAG, "No se pudieron guardar los datos");
                                                    Toast.makeText(getApplicationContext(), "Error al guardar la información", Toast.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                Log.d(TAG, "No se pudieron obtener los datos");
                                                Toast.makeText(getApplicationContext(), "Error al obtener la información", Toast.LENGTH_SHORT).show();
                                            }

                                        } else {
                                            Log.d(TAG, "Resultado inválido #1");
                                            Toast.makeText(getApplicationContext(), object_resultado.getString("mensage"), Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        Log.d(TAG, "Resultado inválido #2");
                                        Toast.makeText(getApplicationContext(), "Error al procesar su solicitud, reintente más tarde y valide el acceso a internet", Toast.LENGTH_LONG).show();
                                        // No contiene una respuesta válida
                                    }


                                } else {
                                    // No viene comercio por que el codigo está mal
                                    Log.d(TAG, "Resultado inválido #3");
                                    Toast.makeText(getApplicationContext(), "Error al procesar solicitud de registro", Toast.LENGTH_SHORT).show();
                                }

                            } else {
                                Log.d(TAG, "Error al traer los datos" + object.optJSONObject("error").toString());
                                (Toast.makeText(getApplicationContext(), "Error al procesar registro" + object.optJSONObject("error").toString(), Toast.LENGTH_SHORT)).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        /*if (tieneAcceso) {
                            activarPermisoAlmacWrite();
                        } else {
                            //(Toast.makeText(getApplicationContext(), "Acceso denegado #1", Toast.LENGTH_SHORT)).show();
                        }*/
                        requestQueue.stop();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Log.d(TAG, );
                        Log.e(TAG, error.toString());

                        if (error instanceof TimeoutError) {
                            (Toast.makeText(getApplicationContext(), "Timeout", Toast.LENGTH_SHORT)).show();
                        } else if (error instanceof NoConnectionError) {
                            (Toast.makeText(getApplicationContext(), "Sin conexión", Toast.LENGTH_SHORT)).show();
                        } else if (error instanceof AuthFailureError) {
                            (Toast.makeText(getApplicationContext(), "Falló al autenticar", Toast.LENGTH_SHORT)).show();
                        } else if (error instanceof ServerError) {
                            (Toast.makeText(getApplicationContext(), "Error de servidor", Toast.LENGTH_SHORT)).show();
                        } else if (error instanceof NetworkError) {
                            (Toast.makeText(getApplicationContext(), "Error de Red", Toast.LENGTH_SHORT)).show();
                        } else if (error instanceof ParseError) {
                            (Toast.makeText(getApplicationContext(), "Error de parseo", Toast.LENGTH_SHORT)).show();
                        } else {
                            (Toast.makeText(getApplicationContext(), "Acceso denegado #2", Toast.LENGTH_SHORT)).show();
                        }
                    }
                }
        ) {
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
        requestQueue.add(getRequest);
    }

    public static void getMunicipios(final Context context, int id_estado){

        StringRequest requestGetMunicipios;
        String URL = Constantes.URL + "/municipios/" + id_estado;

        final RequestQueue requestQueue = Volley.newRequestQueue(context);

        requestGetMunicipios = new StringRequest(Request.Method.GET, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i(TAG, "La respuesta al obtener los municipios es: " + response);

                try{
                    JSONObject jsonResponse = new JSONObject(response);

                    JSONArray ids_municipios = jsonResponse.optJSONArray("id_municipios");
                    JSONArray nombre_municipios = jsonResponse.optJSONArray("nombre_municipio");

                    misMunicipios = new ArrayList<String>();
                    misIDsMunicipios = new ArrayList<String>();

                    for (int i = 0; i < nombre_municipios.length(); i++){
                        misMunicipios.add(nombre_municipios.getString(i));
                        misIDsMunicipios.add(ids_municipios.getString(i));
                    }
                    spMunicipio.setAdapter( new ArrayAdapter<>(context, android.R.layout.simple_selectable_list_item, misMunicipios));
                }
                catch(JSONException e){
                    Toast.makeText(context, "Error en municipios"+e.toString(), Toast.LENGTH_SHORT).show();
                }
                requestQueue.stop();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorResp = "Error #11: " + R.string.error_desconocido;

                if (error instanceof TimeoutError) {
                    errorResp = "Error #11A: " + R.string.error_tiempo_agotado;
                } else if (error instanceof NoConnectionError) {
                    errorResp = "Error #11B: " + R.string.error_sin_conexion;
                } else if (error instanceof AuthFailureError) {
                    errorResp = "Error #11C: " + R.string.error_fallo_autenticar;
                } else if (error instanceof ServerError) {
                    errorResp = "Error #11D: " + R.string.error_servidor;
                } else if (error instanceof NetworkError) {
                    errorResp = "Error #11E: " + R.string.error_red;
                } else if (error instanceof ParseError) {
                    errorResp = "Error #11F: " + R.string.error_parseo;
                }

                Log.e(TAG, errorResp);
                Log.e(TAG, error.getMessage());

                requestQueue.stop();
            }
        });

        requestQueue.add(requestGetMunicipios);
    }

    public static void getLocalidades(final Context context, int id_municipio){

        StringRequest requestGetLocalidades;
        String URL = Constantes.URL + "/localidades/" + id_municipio;

        final RequestQueue requestQueue = Volley.newRequestQueue(context);

        requestGetLocalidades = new StringRequest(Request.Method.GET, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONObject jsonResponse = new JSONObject(response);

                    JSONArray ids_localidades = jsonResponse.optJSONArray("id_localidades");
                    JSONArray nombre_localidades = jsonResponse.optJSONArray("nombre_localidad");

                    misLocalidades.clear();
                    misIDsLocalidades.clear();

                    for (int i = 0; i < nombre_localidades.length(); i++){
                        misLocalidades.add(nombre_localidades.getString(i));
                        misIDsLocalidades.add(ids_localidades.getString(i));
                    }
                    spLocalidad.setAdapter( new ArrayAdapter<>(context, android.R.layout.simple_selectable_list_item, misLocalidades));
                }
                catch(JSONException e){
                    Toast.makeText(context, "Error en municipios"+e.toString(), Toast.LENGTH_SHORT).show();
                }
                requestQueue.stop();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorResp = "Error #12: " + R.string.error_desconocido;

                if (error instanceof TimeoutError) {
                    errorResp = "Error #12: " + R.string.error_tiempo_agotado;
                } else if (error instanceof NoConnectionError) {
                    errorResp = "Error #12: " + R.string.error_sin_conexion;
                } else if (error instanceof AuthFailureError) {
                    errorResp = "Error #12: " + R.string.error_fallo_autenticar;
                } else if (error instanceof ServerError) {
                    errorResp = "Error #12: " + R.string.error_servidor;
                } else if (error instanceof NetworkError) {
                    errorResp = "Error #12: " + R.string.error_red;
                } else if (error instanceof ParseError) {
                    errorResp = "Error #12: " + R.string.error_parseo;
                }
                Log.e(TAG, errorResp);
                requestQueue.stop();
            }
        });

        requestQueue.add(requestGetLocalidades);
    }

    public void mostrarDatePicker(){
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                RegistroActivity.this,
                this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker datePicker, int anio, int mes, int dia) {
        dateNacimiento.setText(dia + "/" + (mes +1) + "/" + anio);
    }

    private void iniciarMain(){
        Intent intent = new Intent(RegistroActivity.this, MainActivity.class);
        startActivity(intent);
    }

    private void activarPermisoAlmacWrite(){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constantes.MY_PERMISSIONS_REQUEST_ALMAC_WRITE);
    }

    private void activarPermisoCam(){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, Constantes.MY_PERMISSIONS_REQUEST_CAMERA);
    }

    private void activarPermisoMicrof(){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, Constantes.MY_PERMISSIONS_REQUEST_MICROF);
    }

    private void activarPermisoUbic(){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Constantes.MY_PERMISSIONS_REQUEST_UBICAC);
    }






    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constantes.MY_PERMISSIONS_REQUEST_ALMAC_WRITE: {
                activarPermisoCam();
                return;
            }
            case Constantes.MY_PERMISSIONS_REQUEST_CAMERA: {
                activarPermisoMicrof();
                return;
            }
            case Constantes.MY_PERMISSIONS_REQUEST_MICROF: {
                activarPermisoUbic();
                return;
            }
            case Constantes.MY_PERMISSIONS_REQUEST_UBICAC: {
                iniciarMain();
                return;
            }
        }
    }
}