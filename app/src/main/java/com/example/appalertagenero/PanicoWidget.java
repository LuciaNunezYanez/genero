package com.example.appalertagenero;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.example.appalertagenero.Servicios.ServicioWidget;

import java.lang.ref.WeakReference;

/**
 * Implementation of App Widget functionality.
 */

public class PanicoWidget extends AppWidgetProvider {

    static WeakReference<Context> contextoGlobal;
    int reporteCreado;
    int idComercio;
    int idUsuario;

    static String TAG = "ServicioWidget";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        // Calcular la imagen a utilizar cuando se cambia el tamaño del widget
        Bundle option = appWidgetManager.getAppWidgetOptions(appWidgetId);
        float ancho = option.getInt(appWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        float alto = option.getInt(appWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
        contextoGlobal = new WeakReference<>(context);

        int imagen;
        int diseno;
        int imagenChica = R.drawable.sos_chica;
        //int layoutMediano = R.layout.panico_widget2;
        int layoutChico = R.layout.panico_widget;
        diseno = layoutChico;

        // (Toast.makeText(context, "Alto: " +  alto + " Ancho: " + ancho + "Dif: " + (alto - ancho), Toast.LENGTH_LONG)).show();
        // Calcular las medidas del Widget para mostrar X diseño
        /*if ( (alto - ancho) <= 30 && (alto - ancho) >= -75) { // Es cuadrado
            imagen = imagenChica;
            diseno = layoutChico;
        } else {
            imagen = imagenMediana;
            diseno = layoutMediano;
        }*/

        // Muestra la notificación
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent intentN = new Intent(context, ServicioWidget.class);
            PendingIntent pendingIntent = PendingIntent.getForegroundService(context, 0, intentN, 0);

            RemoteViews views = new RemoteViews(context.getPackageName(), diseno);
            views.setOnClickPendingIntent(R.id.btnAlertarWidget, pendingIntent);
            appWidgetManager.updateAppWidget(appWidgetId, views);

            //Toast.makeText(context, "Se creó en >28 ", Toast.LENGTH_LONG).show();
            // Iniciar generar alerta

        } else {
            Intent intentN = new Intent(context, ServicioWidget.class);
            PendingIntent pendingIntent = PendingIntent.getService(context, 0, intentN, 0);

            RemoteViews views = new RemoteViews(context.getPackageName(), diseno);
            views.setOnClickPendingIntent(R.id.btnAlertarWidget, pendingIntent);
            appWidgetManager.updateAppWidget(appWidgetId, views);

            //Toast.makeText(context, "Se creó en <28 ", Toast.LENGTH_LONG).show();

        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        updateAppWidget(context, appWidgetManager, appWidgetId);
    }

    @Override
    public void onEnabled(Context context) {
    }

    @Override
    public void onDisabled(Context context) {
    }
}

