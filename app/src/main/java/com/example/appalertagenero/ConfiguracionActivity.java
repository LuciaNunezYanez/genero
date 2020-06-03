package com.example.appalertagenero;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import com.example.appalertagenero.Servicios.ServicioNotificacion;
import com.example.appalertagenero.Utilidades.PreferencesCiclo;
import com.example.appalertagenero.Utilidades.Utilidades;

public class ConfiguracionActivity extends AppCompatActivity {

    private Switch switchServicioActivo;
    private Boolean isActive = false;
    private ImageButton btnSaveCiclo;
    private EditText txtNoCiclo;
    private String TAG = "Configuracion";

    ImageButton btnAlmacWrite, btnCam, btnMicrof, btnUbic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);
        switchServicioActivo = findViewById(R.id.switchServicioActivo);
        btnSaveCiclo = findViewById(R.id.iBtnGuardarCiclo);
        txtNoCiclo = findViewById(R.id.txtNoCiclo);

        obtenerPreferenciasNotificacion();
        switchServicioActivo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    iniciarServicioPersistente();
                } else {
                    detenerServicioPersistente();
                }
            }
        });

        obtenerPreferenciasCiclo();
        btnSaveCiclo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    if (txtNoCiclo.getText()== null || Integer.parseInt(txtNoCiclo.getText().toString()) <= 0 ){
                        Toast.makeText(getApplication(), "¡Por favor ingrese un valor válido!" , Toast.LENGTH_SHORT).show();
                    } else if ( Integer.parseInt(txtNoCiclo.getText().toString()) > 3  ){
                        Toast.makeText(getApplication(), "¡Por favor ingrese un valor entre 1 y 3!" , Toast.LENGTH_SHORT).show();
                    } else if (Integer.parseInt(txtNoCiclo.getText().toString()) >= 1 && Integer.parseInt(txtNoCiclo.getText().toString()) <= 3){
                        guardarPreferenciasCiclo(Integer.parseInt(txtNoCiclo.getText().toString()));
                    }
                }catch (Exception e){
                    Toast.makeText(getApplication(), "¡Por favor ingrese un valor válido!" , Toast.LENGTH_SHORT).show();
                }


            }
        });

        // PERMISOS
        btnAlmacWrite = findViewById(R.id.btnAlmacenamientoWrite);
        btnCam = findViewById(R.id.btnCamara);
        btnMicrof = findViewById(R.id.btnMicrofono);
        btnUbic = findViewById(R.id.btnUbicacion);

        // Detectar permisos
        permisoAlmacWrite();
        permisoCam();
        permisoMicrof();
        permisoUbic();
    }

    private void obtenerPreferenciasNotificacion(){
        SharedPreferences preferences = getApplication().getSharedPreferences("NotificacionPersistente", Context.MODE_PRIVATE);
        if (preferences.contains("notificacionActiva")){
            isActive = Utilidades.isMyServiceRunning(getApplication(), ServicioNotificacion.class);
            switchServicioActivo.setChecked(isActive);
        } else {
            isActive = Utilidades.isMyServiceRunning(getApplication(), ServicioNotificacion.class);
            switchServicioActivo.setChecked(isActive);
        }
    }

    private void obtenerPreferenciasCiclo(){
        PreferencesCiclo preferencesCiclo = new PreferencesCiclo();
        int ciclos = preferencesCiclo.obtenerCicloFotografias(getApplication());
        txtNoCiclo.setText(String.valueOf(ciclos));
    }

    private void guardarPreferenciasCiclo(int ciclo){
        PreferencesCiclo preferencesCiclo = new PreferencesCiclo();
        Boolean res = preferencesCiclo.guardarCicloFotografias(getApplication(), ciclo);
        if(res){
            Toast.makeText(getApplication(), "¡Número de ciclos guardados con éxito!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplication(), "¡Error al guardar el número de ciclos!", Toast.LENGTH_SHORT).show();
        }
    }

    public void iniciarServicioPersistente(){
        Intent notificationIntent = new Intent(getApplication(), ServicioNotificacion.class);
        notificationIntent.putExtra("padre", "App");
        getApplication().startService(notificationIntent);
        isActive = Utilidades.isMyServiceRunning(getApplication(), ServicioNotificacion.class);
        actualizarPreferenciasNotificacion(isActive);
    }

    public void detenerServicioPersistente(){
        Intent notificationIntent = new Intent(getApplication(), ServicioNotificacion.class);
        getApplication().stopService(notificationIntent);
        isActive = Utilidades.isMyServiceRunning(getApplication(), ServicioNotificacion.class);
        actualizarPreferenciasNotificacion(isActive);
    }

    private void actualizarPreferenciasNotificacion(boolean nuevoValor){
        SharedPreferences preferences = getApplication().getSharedPreferences("NotificacionPersistente", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("notificacionActiva", nuevoValor);
        editor.commit();
    }


    private void permisoAlmacWrite(){
        if(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            btnAlmacWrite.setBackgroundColor(Color.rgb(65, 174, 71));
        } else {
            btnAlmacWrite.setBackgroundColor(Color.rgb(213, 34, 37));
            Log.e(TAG, "No tiene permisos Write");
        }
    }

    private void permisoCam(){
        if(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
            btnCam.setBackgroundColor(Color.rgb(65, 174, 71));
        } else {
            btnCam.setBackgroundColor(Color.rgb(213, 34, 37));
            Log.e(TAG, "No tiene permisos camara");
        }
    }

    private void permisoMicrof(){
        if(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED){
            btnMicrof.setBackgroundColor(Color.rgb(65, 174, 71));
        } else {
            btnMicrof.setBackgroundColor(Color.rgb(213, 34, 37));
            Log.e(TAG, "No tiene permisos microfono");
        }
    }

    private void permisoUbic(){
        if(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            btnUbic.setBackgroundColor(Color.rgb(65, 174, 71));
        } else {
            btnUbic.setBackgroundColor(Color.rgb(213, 34, 37));
            Log.e(TAG, "No tiene permisos ubicacion");
        }
    }

    public void activarPermisoAlmacWrite(View view){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constantes.MY_PERMISSIONS_REQUEST_ALMAC_WRITE);
    }

    public void activarPermisoCam(View view){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, Constantes.MY_PERMISSIONS_REQUEST_CAMERA);
    }

    public void activarPermisoMicrof(View view){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, Constantes.MY_PERMISSIONS_REQUEST_MICROF);
    }

    public void activarPermisoUbic(View view){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Constantes.MY_PERMISSIONS_REQUEST_UBICAC);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constantes.MY_PERMISSIONS_REQUEST_ALMAC_WRITE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "PERMISO DE ESCRITURA ACEPTADO");
                    permisoAlmacWrite();
                } else {
                    Toast.makeText(getApplicationContext(), "¡Permiso de escritura denegado!", Toast.LENGTH_LONG).show();
                }
                return;
            }
            case Constantes.MY_PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "PERMISO DE CAMARA ACEPTADO");
                    permisoCam();
                } else {
                    Toast.makeText(getApplicationContext(), "¡Permiso de cámara denegado!", Toast.LENGTH_LONG).show();
                }
                return;
            }
            case Constantes.MY_PERMISSIONS_REQUEST_MICROF: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "PERMISO DE MICROFONO ACEPTADO");
                    permisoMicrof();
                } else {
                    Toast.makeText(getApplicationContext(), "¡Permiso de micrófono denegado!", Toast.LENGTH_LONG).show();
                }
                return;
            }
            case Constantes.MY_PERMISSIONS_REQUEST_UBICAC: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "PERMISO DE UBICACION ACEPTADO");
                    permisoUbic();
                } else {
                    Toast.makeText(getApplicationContext(), "¡Permiso de ubicación denegado!", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

}