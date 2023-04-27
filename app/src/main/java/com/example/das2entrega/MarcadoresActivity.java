package com.example.das2entrega;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import java.io.FileInputStream;
import java.util.ArrayList;

public class MarcadoresActivity extends AppCompatActivity {
    String usuario;
    private String[] marcadoresString = new String[20];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marcadores);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            usuario = extras.getString("usuario");
        }

        getMarcadores();

        //lista que contiene los marcadores. Si clicamos en un marcador, se elimina
        ListView listaMarcadores = (ListView) findViewById(R.id.listaMarcadores);
        listaMarcadores.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String marcador = (String) listaMarcadores.getItemAtPosition(position);
                eliminarMarcador(marcador);
            }
        });

        // boton añadir marcador
        Button botonAñadirMarcador = (Button) findViewById(R.id.botonAñadirMarcador);
        botonAñadirMarcador.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickAñadirMarcador(view);
            }
        });

        //boton ver mapa
        Button botonVerMapa = (Button) findViewById(R.id.botonMapa);
        botonVerMapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickVerMapa(view);
            }
        });
    }

    public void getImagenPerfil() {
        FileInputStream ficheroImagen = null;
        try {
            ficheroImagen = openFileInput(usuario + ".jpg");
            if(ficheroImagen != null && ficheroImagen.available() > 0) {
                Bitmap imagenBitmap = BitmapFactory.decodeStream(ficheroImagen);
                ImageView elImageView = findViewById(R.id.imagenPerfil);
                elImageView.setImageBitmap(imagenBitmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ImageView elImageView = findViewById(R.id.imagenPerfil);
            elImageView.setImageResource(R.drawable.ic_launcher_foreground);
        }
    }

    public void getMarcadores() {
        // añadir los marcadores a la variable para poder visualizarlos
        // programar tarea para recoger los marcadores de BD
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
                                // guardar los datos en la variable y visualizarlo en la lista
                                marcadoresString = datos.split(";");

                                ListView listaMarcadores = (ListView) findViewById(R.id.listaMarcadores);
                                ArrayAdapter eladaptador = new ArrayAdapter<String> (getApplicationContext(), android.R.layout.simple_list_item_1, marcadoresString);
                                listaMarcadores.setAdapter(eladaptador);
                            } else {
                                ListView listaMarcadores = (ListView) findViewById(R.id.listaMarcadores);
                                ArrayAdapter eladaptador = new ArrayAdapter<String> (getApplicationContext(), android.R.layout.simple_list_item_1, android.R.id.empty);
                                listaMarcadores.setAdapter(eladaptador);
                            }
                        }
                    }
                });
        WorkManager.getInstance(this).enqueue(otwrGetMarcadores);
    }

    public void onClickAñadirMarcador(View view) {
        String latitud = ((TextView) findViewById(R.id.editTextLatitud)).getText().toString();
        String longitud = ((TextView) findViewById(R.id.editTextLongitud)).getText().toString();
        String marcadorString = latitud + ", " + longitud;

        // programar worker
        Data datos = new Data.Builder().putString("usuario", usuario).putString("marcador", marcadorString).build();
        OneTimeWorkRequest otwrSubirMarcador = new OneTimeWorkRequest.Builder(TareaSubirMarcadorBD.class).setInputData(datos).build();
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(otwrSubirMarcador.getId())
                .observe(this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        if(workInfo != null && workInfo.getState().isFinished()){
                            String datos = workInfo.getOutputData().getString("datos");
                            System.out.println(datos);
                            if (datos.equalsIgnoreCase("Se ha subido el marcador correctamente")) {
                                // guardar los datos en la variable y visualizarlo en la lista
                                getMarcadores();
                            }
                        }
                    }
                });
        WorkManager.getInstance(this).enqueue(otwrSubirMarcador);
    }

    public void eliminarMarcador(String marcador) {
        // programar worker para eliminar
        Data datos = new Data.Builder().putString("usuario", usuario).putString("marcador", marcador).build();
        OneTimeWorkRequest otwrEliminarMarcador = new OneTimeWorkRequest.Builder(TareaEliminarMarcadorBD.class).setInputData(datos).build();
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(otwrEliminarMarcador.getId())
                .observe(this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        if(workInfo != null && workInfo.getState().isFinished()){
                            String datos = workInfo.getOutputData().getString("datos");
                            System.out.println(datos);
                            if (datos.equalsIgnoreCase("Se ha eliminado el marcador correctamente")) {
                                // guardar los datos en la variable y visualizarlo en la lista
                                getMarcadores();
                            }
                        }
                    }
                });
        WorkManager.getInstance(this).enqueue(otwrEliminarMarcador);
    }

    public void onClickVerMapa(View view) {
        Intent intent = new Intent(getApplicationContext(), MapaActivity.class);
        intent.putExtra("usuario", usuario);
        setResult(RESULT_OK, intent);
        startActivity(intent);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("usuario", usuario);
        outState.putStringArray("marcadoresString", marcadoresString);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        marcadoresString = savedInstanceState.getStringArray("marcadoresString");
        usuario = savedInstanceState.getString("usuario");
        ListView listaMarcadores = (ListView) findViewById(R.id.listaMarcadores);
        ArrayAdapter eladaptador = new ArrayAdapter<String> (getApplicationContext(), android.R.layout.simple_list_item_1, marcadoresString);
        listaMarcadores.setAdapter(eladaptador);
    }
}
