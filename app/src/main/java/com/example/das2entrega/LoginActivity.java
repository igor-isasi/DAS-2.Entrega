package com.example.das2entrega;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

public class LoginActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // solicitar permisos de notificaciones
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)!= PackageManager.PERMISSION_GRANTED) {
                //PEDIR EL PERMISO
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 11);
            }
        }

        // si el modo background esta deshabilitado, pedir que se habilite para que se reciban los mensajes FCM
        ActivityManager am= (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // se crea y se activa la notificacion en caso de que no este activado el modo background
            if (am.isBackgroundRestricted()==true){
                NotificationCompat.Builder elBuilder = new NotificationCompat.Builder(this, "Activar_background")
                        .setContentTitle("Modo background")
                        .setContentText("Por favor, active el modo background para poder recibir mensajes FCM")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                NotificationManager elManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel elCanal = new NotificationChannel("Activar_background", "Canal_principal", NotificationManager.IMPORTANCE_DEFAULT);
                    elCanal.setDescription("Canal principal");
                    elCanal.enableLights(true);
                    elCanal.setLightColor(Color.YELLOW);
                    elCanal.setVibrationPattern(new long[]{0, 1000, 500, 1000});
                    elCanal.enableVibration(true);
                    elManager.createNotificationChannel(elCanal);
                }
                elManager.notify(1, elBuilder.build());
            }
        }

        //boton iniciar sesion
        Button botonIniciarSesion = (Button) findViewById(R.id.botonIniciarSesion);
        botonIniciarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickIniciarSesion(view);
            }
        });

        //boton registro
        Button botonRegistro = (Button) findViewById(R.id.botonRegistro);
        botonRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickRegistro(view);
            }
        });
    }

    public void onClickIniciarSesion(View view) {
        String usuario = ((TextView)findViewById(R.id.textUsuario)).getText().toString();
        String contraseña = ((TextView)findViewById(R.id.textContraseña)).getText().toString();
        // preparar variable de tipo Data para pasar los datos a la tarea
        Data datos = new Data.Builder().putString("usuario", usuario).putString("contraseña", contraseña).build();
        if (!usuario.isEmpty() && !contraseña.isEmpty()) {
            // recoger usuarios de la BD
            OneTimeWorkRequest otwrGet = new OneTimeWorkRequest.Builder(TareaLoginBD.class).setInputData(datos).build();
            WorkManager.getInstance(this).getWorkInfoByIdLiveData(otwrGet.getId())
                    .observe(this, new Observer<WorkInfo>() {
                        @Override
                        public void onChanged(WorkInfo workInfo) {
                            if(workInfo != null && workInfo.getState().isFinished()){
                                String datos = workInfo.getOutputData().getString("datos");
                                if (datos.equalsIgnoreCase("Login correcto")) {
                                    Toast aviso = Toast.makeText(getApplicationContext(), datos, Toast.LENGTH_SHORT);
                                    aviso.show();
                                    Intent intent = new Intent(getApplicationContext(), PerfilActivity.class);
                                    intent.putExtra("usuario", usuario);
                                    setResult(RESULT_OK, intent);
                                    startActivity(intent);
                                } else if (datos.equalsIgnoreCase("Contraseña incorrecta")) {
                                    Toast aviso = Toast.makeText(getApplicationContext(), datos, Toast.LENGTH_SHORT);
                                    aviso.show();
                                } else if (datos.equalsIgnoreCase("Usuario no encontrado")) {
                                    Toast aviso = Toast.makeText(getApplicationContext(), datos, Toast.LENGTH_SHORT);
                                    aviso.show();
                                } else {
                                    Toast aviso = Toast.makeText(getApplicationContext(), "Ha ocurrido un problema", Toast.LENGTH_SHORT);
                                    aviso.show();
                                }
                            }
                        }
                    });
            WorkManager.getInstance(this).enqueue(otwrGet);
        } else {
            Toast aviso = Toast.makeText(getApplicationContext(), "Por favor, rellena el usuario y la contraseña", Toast.LENGTH_SHORT);
            aviso.show();
        }
    }

    public void onClickRegistro(View view) {
        String usuario = ((TextView) findViewById(R.id.textUsuario)).getText().toString();
        String contraseña = ((TextView) findViewById(R.id.textContraseña)).getText().toString();
        // preparar variable de tipo Data para pasar los datos a la tarea
        Data datos = new Data.Builder().putString("usuario", usuario).putString("contraseña", contraseña).build();
        if (!usuario.isEmpty() && contraseña.length() >= 5) {
            // subir usuarios a la BD
            OneTimeWorkRequest otwrPost = new OneTimeWorkRequest.Builder(TareaRegistroBD.class).setInputData(datos).build();
            WorkManager.getInstance(this).getWorkInfoByIdLiveData(otwrPost.getId())
                    .observe(this, new Observer<WorkInfo>() {
                        @Override
                        public void onChanged(WorkInfo workInfo) {
                            if (workInfo != null && workInfo.getState().isFinished()) {
                                String datos = workInfo.getOutputData().getString("datos");
                                if (datos.equalsIgnoreCase("Se ha procesado el registro correctamente")) {
                                    Toast aviso = Toast.makeText(getApplicationContext(), datos, Toast.LENGTH_SHORT);
                                    aviso.show();
                                    Intent intent = new Intent(getApplicationContext(), PerfilActivity.class);
                                    intent.putExtra("usuario", usuario);
                                    setResult(RESULT_OK, intent);
                                    startActivity(intent);
                                } else {
                                    Toast aviso = Toast.makeText(getApplicationContext(), datos, Toast.LENGTH_SHORT);
                                    aviso.show();
                                }
                            }
                        }
                    });
            WorkManager.getInstance(this).enqueue(otwrPost);
        } else if (!contraseña.isEmpty()) {
            Toast aviso = Toast.makeText(getApplicationContext(), "La contraseña debe tener 5 caracteres por lo menos", Toast.LENGTH_SHORT);
            aviso.show();
        } else {
            Toast aviso = Toast.makeText(getApplicationContext(), "Por favor, rellena el usuario y la contraseña", Toast.LENGTH_SHORT);
            aviso.show();
        }
    }
}