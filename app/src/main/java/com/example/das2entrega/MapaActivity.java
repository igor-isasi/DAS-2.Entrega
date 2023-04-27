package com.example.das2entrega;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Calendar;

public class MapaActivity extends FragmentActivity implements OnMapReadyCallback {
    String[] marcadoresString = new String[20];
    String usuario = null;
    String token =  null;
    Alarma alarma = new Alarma();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            usuario = extras.getString("usuario");
        }

        // recoger token
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            task.getException().printStackTrace();
                            return;
                        }
                        token = task.getResult();

                        Data datos = new Data.Builder().putString("token", token).build();
                        OneTimeWorkRequest otwrFCM = new OneTimeWorkRequest.Builder(TareaFCM.class).setInputData(datos).build();
                        WorkManager.getInstance(MapaActivity.this).getWorkInfoByIdLiveData(otwrFCM.getId())
                                .observe(MapaActivity.this, new Observer<WorkInfo>() {
                                    @Override
                                    public void onChanged(WorkInfo workInfo) {
                                        if (workInfo != null && workInfo.getState().isFinished()) {
                                            String datos = workInfo.getOutputData().getString("datos");
                                            System.out.println(datos);
                                        }
                                    }
                                });
                        WorkManager.getInstance(MapaActivity.this).enqueue(otwrFCM);
                    }
                });

        // preparar variable de tipo Data para pasar los datos a la tarea
        Data datos = new Data.Builder().putString("token", token).build();
        if (token != null) {
            OneTimeWorkRequest otwrFCM = new OneTimeWorkRequest.Builder(TareaFCM.class).setInputData(datos).build();
            WorkManager.getInstance(this).getWorkInfoByIdLiveData(otwrFCM.getId())
                    .observe(this, new Observer<WorkInfo>() {
                        @Override
                        public void onChanged(WorkInfo workInfo) {
                            if (workInfo != null && workInfo.getState().isFinished()) {
                                String datos = workInfo.getOutputData().getString("datos");
                                System.out.println(datos);
                            }
                        }
                    });
            WorkManager.getInstance(this).enqueue(otwrFCM);
        }

        alarma.programarAlarma(this);

        // Recoger los marcadores de la BD
        getMarcadores();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        GoogleMap elMapa = googleMap;
        elMapa.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Acceder a la ubiacion actual del movil y centrar el mapa en la ubicacion
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            FusedLocationProviderClient proveedordelocalizacion =
                    LocationServices.getFusedLocationProviderClient(this);
            proveedordelocalizacion.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                System.out.println(location.getLatitude());
                                System.out.println(location.getLongitude());
                                elMapa.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("Ubicación actual"));
                                CameraUpdate actualizar = CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()),9);
                                elMapa.moveCamera(actualizar);
                            } else {
                                System.out.println("Ha surgido un problema al intenter obtener la ubicación actual");
                            }
                        }
                    })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            e.printStackTrace();
                        }
                    });
        } else {
            Toast aviso = Toast.makeText(getApplicationContext(), "No hay permiso para la geolocalizacion", Toast.LENGTH_SHORT);
            aviso.show();
        }

        // Añadir los marcadores obtenidos de BD al mapa
        for (String marcadorString: marcadoresString) {
            if (marcadorString != null) {
                Double latitud = Double.parseDouble(marcadorString.split(",")[0]);
                Double longitud = Double.parseDouble(marcadorString.split(", ")[1]);
                elMapa.addMarker(new MarkerOptions().position(new LatLng(latitud, longitud)));
            }
        }
    }

    public void getMarcadores() {
        // Se recogen los marcadores de BD mediante una tarea
        Data datos = new Data.Builder().putString("usuario", usuario).build();
        OneTimeWorkRequest otwrGetMarcadores = new OneTimeWorkRequest.Builder(TareaGetMarcadoresBD.class).setInputData(datos).build();
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(otwrGetMarcadores.getId())
                .observe(this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        if(workInfo != null && workInfo.getState().isFinished()){
                            String datos = workInfo.getOutputData().getString("datos");
                            System.out.println(datos);
                            if (!datos.equalsIgnoreCase("No hay marcadores")) {
                                // guardar los datos en la variable
                                marcadoresString = datos.split(";");
                                SupportMapFragment elFragmento = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentoMapa);
                                elFragmento.getMapAsync(MapaActivity.this);
                            } else {
                                SupportMapFragment elFragmento = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentoMapa);
                                elFragmento.getMapAsync(MapaActivity.this);
                            }
                        }
                    }
                });
        WorkManager.getInstance(this).enqueue(otwrGetMarcadores);
    }
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("usuario", usuario);
        outState.putStringArray("marcadoresString", marcadoresString);
        outState.putString("token", token);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        marcadoresString = savedInstanceState.getStringArray("marcadoresString");
        usuario = savedInstanceState.getString("usuario");
        token = savedInstanceState.getString("token");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        alarma.cancelarAlarma(this);
    }
}
