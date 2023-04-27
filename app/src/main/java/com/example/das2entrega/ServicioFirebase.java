package com.example.das2entrega;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class ServicioFirebase extends FirebaseMessagingService {

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
    }

    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            // recibimos la hora del server la cual está 2 horas retrasada respecto a la de aqui, por eso hay que añadirle dos horas
            String horaServer = remoteMessage.getData().get("hora");
            String hora = String.valueOf(Integer.parseInt(horaServer.split(":")[0])+2) + ":" + horaServer.split(":")[1];

            //crear el manager y el builder de las notificaciones locales
            NotificationCompat.Builder elBuilder = new NotificationCompat.Builder(this, "Hora_ubi")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("Tu ubicación")
                    .setContentText("Hora en la que se ha tomado tu ubicación: " + hora)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            NotificationManager elManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel elCanal = new NotificationChannel("Hora_ubi", "Canal_principal", NotificationManager.IMPORTANCE_DEFAULT);
                elCanal.setDescription("Canal principal");
                elCanal.enableLights(true);
                elCanal.setLightColor(Color.YELLOW);
                elCanal.setVibrationPattern(new long[]{0, 1000, 500, 1000});
                elCanal.enableVibration(true);
                elManager.createNotificationChannel(elCanal);
            }

            elManager.notify(1, elBuilder.build());
        }
        if (remoteMessage.getNotification() != null) {
            System.out.println(remoteMessage.getNotification().getBody());
        }
    }
}
