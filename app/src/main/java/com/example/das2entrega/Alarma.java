package com.example.das2entrega;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.widget.Toast;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class Alarma extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Cuando "suene" la alarma, sale un toast para avisar de que han pasado ya 5 minutos desde que se tomó la ubiación
        Toast aviso = Toast.makeText(context, "Han pasado 5 minutos desde que se tomó tu ubicación.", Toast.LENGTH_SHORT);
        aviso.show();
    }

    public void programarAlarma(Context context) {
        // Programar alarma para dentro de 5 minutos
        Intent i= new Intent(context, Alarma.class);
        PendingIntent i2 = PendingIntent.getBroadcast(context, 0, i, 0);
        AlarmManager gestor= (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        gestor.set(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis() + TimeUnit.MINUTES.toMillis(5),i2);
    }

    public void cancelarAlarma(Context context) {
        Intent i= new Intent(context, Alarma.class);
        PendingIntent i2 = PendingIntent.getBroadcast(context, 0, i, 0);
        AlarmManager gestor= (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        gestor.cancel(i2);
    }
}
