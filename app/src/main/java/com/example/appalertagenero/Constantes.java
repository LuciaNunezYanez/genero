package com.example.appalertagenero;

public class Constantes {

    //     1,000 1 seg
    //    60,000 1 minuto
    // 3,600,000 1 hora

    public static final String CHANNEL_ID = "BOTON_ALERTA_GENERO";
    public static final int PRIORITY_MAX = 2;
    public static final int ID_SERVICIO_PANICO = 100;
    public static final int ID_SERVICIO_AUDIO = 102;
    public static final int ID_SERVICIO_WIDGET = 101;

    public static final int ID_SERVICIO_WIDGET_CREAR_REPORTE = 200;
    public static final int ID_SERVICIO_WIDGET_GENERAR_ALERTA = 201;
    public static final int ID_SERVICIO_WIDGET_ENVIAR_IMG = 202;
    public static final int ID_SERVICIO_WIDGET_GRABAR_AUDIO = 203;
    public static final int ID_SERVICIO_WIDGET_TOMAR_UBICAC = 204;

    // Permisos
    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 100;
    public static final int MY_PERMISSIONS_REQUEST_ALMAC_READ = 101;
    public static final int MY_PERMISSIONS_REQUEST_ALMAC_WRITE = 102;
    public static final int MY_PERMISSIONS_REQUEST_UBICAC = 103;
    public static final int MY_PERMISSIONS_REQUEST_MICROF = 104;

    //public static String TAG = "Debug";
    // Constantes para reporte
    public static final int DIFERENCIA_ENTRE_REPORTES = 60000; // 60000 = 1 minuto
    public static final int LAPSO_PARA_CANCELAR_REPORTE = 60000; // 60000 = 1 minuto

    // Constantes para audio
    public static int DURACION_AUDIO = 5000; // 15 x 4 = 60 seg
    public static String EXTENSION_AUDIO = "mp3";


    public static String URL = "http://10.11.127.70:8888"; // LOCAL
    // public static String URL = "http://189.254.158.196:8888";    // SERVER
    // public static String URL = "http://10.11.126.114:8888";

    // public static String URL = "http://192.168.0.9:3000";

}
